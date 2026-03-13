package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.dto.HttpPacketDetails;
import com.networkwatcher.network_watcher.model.CapturedPacket;
import com.networkwatcher.network_watcher.dto.DnsQueryDetails;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.DnsPacket;
import org.pcap4j.packet.namednumber.TcpPort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class PacketService {

    private PcapHandle handle;
    private final List<CapturedPacket> capturedPackets = new CopyOnWriteArrayList<>();
    private final List<HttpPacketDetails> httpPackets = new CopyOnWriteArrayList<>();
    private final List<DnsQueryDetails> dnsQueries = new CopyOnWriteArrayList<>();
    private boolean isCapturing = false;

    public void startCapturing(String interfaceIp) throws PcapNativeException, NotOpenException, java.net.UnknownHostException {
        if (isCapturing) {
            return;
        }

        PcapNetworkInterface nif = Pcaps.getDevByAddress(java.net.InetAddress.getByName(interfaceIp));
        if (nif == null) {
            throw new PcapNativeException("Network interface not found for IP: " + interfaceIp);
        }

        int snapLen = 65536;
        PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
        int timeout = 10;
        handle = nif.openLive(snapLen, mode, timeout);

        handle.setFilter("tcp or udp port 53", BpfProgram.BpfCompileMode.OPTIMIZE);

        isCapturing = true;
        
        new Thread(() -> {
            try {
                handle.loop(-1, (PacketListener) packet -> {
                    if (!isCapturing) {
                        try {
                            handle.breakLoop();
                        } catch (NotOpenException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    processPacket(packet);
                });
            } catch (PcapNativeException | InterruptedException | NotOpenException e) {
                e.printStackTrace();
                isCapturing = false;
            }
        }).start();
    }

    public void stopCapturing() {
        isCapturing = false;
        if (handle != null && handle.isOpen()) {
            try {
                handle.breakLoop();
                handle.close();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }
    }

    public List<CapturedPacket> getCapturedPackets() {
        return new ArrayList<>(capturedPackets);
    }
    
    public void clearPackets() {
        capturedPackets.clear();
        httpPackets.clear();
        dnsQueries.clear();
    }

    public List<HttpPacketDetails> getHttpPackets() {
        return new ArrayList<>(httpPackets);
    }
    
    public List<DnsQueryDetails> getDnsQueries() {
        return new ArrayList<>(dnsQueries);
    }
    
    public Map<String, Integer> getAppSummary() {
        Map<String, Integer> counts = new HashMap<>();
        for (HttpPacketDetails h : httpPackets) {
            String app = mapDomainToApp(h.getHost());
            counts.put(app, counts.getOrDefault(app, 0) + 1);
        }
        for (DnsQueryDetails d : dnsQueries) {
            String app = mapDomainToApp(d.getDomain());
            counts.put(app, counts.getOrDefault(app, 0) + 1);
        }
        return counts;
    }

    private void processPacket(Packet packet) {
        if (packet.contains(IpV4Packet.class)) {
            IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
            String srcIp = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
            String dstIp = ipV4Packet.getHeader().getDstAddr().getHostAddress();

            if (packet.contains(UdpPacket.class) && packet.contains(DnsPacket.class)) {
                DnsPacket dp = packet.get(DnsPacket.class);
                if (dp.getHeader().getQuestions() != null && !dp.getHeader().getQuestions().isEmpty()) {
                    String qname = dp.getHeader().getQuestions().get(0).getQName().getName();
                    DnsQueryDetails q = new DnsQueryDetails(qname, srcIp, LocalDateTime.now());
                    if (dnsQueries.size() >= 200) dnsQueries.remove(0);
                    dnsQueries.add(q);
                    CapturedPacket capturedPacket = new CapturedPacket(
                            srcIp, dstIp, "UDP", packet.length(), "DNS " + qname, LocalDateTime.now()
                    );
                    if (capturedPackets.size() >= 100) capturedPackets.remove(0);
                    capturedPackets.add(capturedPacket);
                }
            }

            if (packet.contains(TcpPacket.class)) {
                TcpPacket tcpPacket = packet.get(TcpPacket.class);
                int srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
                int dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();
                
                String info = "TCP " + srcPort + " -> " + dstPort;
                
                if (tcpPacket.getPayload() != null) {
                    String payload = new String(tcpPacket.getPayload().getRawData());
                    if (payload.startsWith("GET") || payload.startsWith("POST") || payload.startsWith("PUT") || payload.startsWith("DELETE")) {
                        info = "HTTP: " + payload.split("\\r\\n")[0];
                        analyzeHttpPacket(payload, srcIp, dstIp);
                    }
                }

                CapturedPacket capturedPacket = new CapturedPacket(
                        srcIp,
                        dstIp,
                        "TCP",
                        packet.length(),
                        info,
                        LocalDateTime.now()
                );

                if (capturedPackets.size() >= 100) {
                    capturedPackets.remove(0);
                }
                capturedPackets.add(capturedPacket);
            }
        }
    }
    
    public boolean isCapturing() {
        return isCapturing;
    }

    private void analyzeHttpPacket(String payload, String srcIp, String dstIp) {
        try {
            String[] lines = payload.split("\\r\\n");
            if (lines.length == 0) return;

            String requestLine = lines[0];
            String[] parts = requestLine.split(" ");
            if (parts.length < 3) return;

            String method = parts[0];
            String url = parts[1];
            
            String host = null;
            String userAgent = null;
            String contentType = null;

            for (String line : lines) {
                if (line.startsWith("Host: ")) {
                    host = line.substring(6).trim();
                } else if (line.startsWith("User-Agent: ")) {
                    userAgent = line.substring(12).trim();
                } else if (line.startsWith("Content-Type: ")) {
                    contentType = line.substring(14).trim();
                }
            }

            HttpPacketDetails details = new HttpPacketDetails(
                method, url, host, userAgent, contentType, srcIp, dstIp, LocalDateTime.now()
            );

            if (httpPackets.size() >= 50) {
                httpPackets.remove(0);
            }
            httpPackets.add(details);
        } catch (Exception e) {
            // Ignore malformed packets
        }
    }
    
    private String mapDomainToApp(String host) {
        if (host == null) return "Other";
        String h = host.toLowerCase(Locale.ROOT);
        if (h.contains("whatsapp")) return "WhatsApp";
        if (h.contains("facebook") || h.contains("fbcdn") || h.contains("instagram")) return "Meta";
        if (h.contains("youtube") || h.contains("googlevideo")) return "YouTube";
        if (h.contains("netflix")) return "Netflix";
        if (h.contains("tiktok")) return "TikTok";
        if (h.contains("snapchat")) return "Snapchat";
        if (h.contains("x.com") || h.contains("twitter")) return "Twitter";
        if (h.contains("microsoft") || h.contains("windowsupdate") || h.contains("live.com")) return "Microsoft";
        if (h.contains("apple") || h.contains("icloud") || h.contains("itunes")) return "Apple";
        if (h.contains("google") || h.contains("gstatic") || h.contains("googleapis")) return "Google";
        return "Other";
    }
}
