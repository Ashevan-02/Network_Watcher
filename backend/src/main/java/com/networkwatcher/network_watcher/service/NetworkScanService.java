package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.dto.ScanResult;
import com.networkwatcher.network_watcher.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.InetAddress;

@Service
@Slf4j
public class NetworkScanService {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private WebSocketNotificationService webSocketService;
    
    @Autowired
    private SnmpService snmpService;

    @Autowired
    private SnmpSettingsService snmpSettingsService;

    public ScanResult scanNetwork(String networkRange) {
        ScanResult result = new ScanResult();
        result.setNetworkRange(networkRange);
        result.setStatus("RUNNING");

        try {
            String command = "nmap -sn -PR -PE -PA22,80,443 -oG - " + networkRange;
            log.info("Executing: {}", command);

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            Pattern ipPattern = Pattern.compile("Host: (\\d+\\.\\d+\\.\\d+\\.\\d+)");

            while ((line = reader.readLine()) != null) {
                Matcher matcher = ipPattern.matcher(line);
                if (matcher.find()) {
                    String ip = matcher.group(1);
                    result.getDiscoveredIps().add(ip);
                    deviceService.createOrUpdateDevice(ip, null, null, null);
                    webSocketService.notifyDeviceDiscovered(ip);
                }
            }

            process.waitFor();
            result.setDevicesFound(result.getDiscoveredIps().size());
            result.setStatus("COMPLETED");
            result.setMessage("Scan completed successfully");
            webSocketService.notifyScanComplete(result.getDevicesFound());
            log.info("Scan completed. Found {} devices", result.getDevicesFound());

            for (String ip : result.getDiscoveredIps()) {
                try {
                    try {
                        InetAddress addr = InetAddress.getByName(ip);
                        String host = addr.getCanonicalHostName();
                        if (host != null && !host.equals(ip)) {
                            var opt = deviceService.getDeviceByIp(ip);
                            if (opt.isPresent()) {
                                var d = opt.get();
                                if (d.getHostname() == null || "Unknown".equalsIgnoreCase(d.getHostname())) {
                                    d.setHostname(host);
                                    d.setHostnameSource("DNS");
                                    deviceService.saveDevice(d);
                                }
                            }
                        }
                    } catch (Exception e) {}

                    try {
                        boolean ok = false;
                        var communities = snmpSettingsService.getCommunities();
                        if (communities.isEmpty()) {
                            ok = snmpService.enrichDevice(ip, "public");
                        } else {
                            for (String c : communities) {
                                if (snmpService.enrichDevice(ip, c)) { ok = true; break; }
                            }
                        }
                    } catch (Exception e) {}

                    try {
                        var optDev = deviceService.getDeviceByIp(ip);
                        if (optDev.isPresent()) {
                            var d0 = optDev.get();
                            if (d0.getHostname() == null || "Unknown".equalsIgnoreCase(d0.getHostname())) {
                                String nb = resolveViaNbtstat(ip);
                                if (nb != null && !nb.isBlank()) {
                                    d0.setHostname(nb);
                                    d0.setHostnameSource("NETBIOS");
                                    deviceService.saveDevice(d0);
                                } else {
                                    String pingName = resolveViaPing(ip);
                                    if (pingName != null && !pingName.equals(ip)) {
                                        d0.setHostname(pingName);
                                        d0.setHostnameSource("DNS");
                                        deviceService.saveDevice(d0);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {}

                    var deviceOpt = deviceService.getDeviceByIp(ip);
                    if (deviceOpt.isPresent()) {
                        var d = deviceOpt.get();
                        boolean hostnameUnknown = (d.getHostname() == null || "Unknown".equalsIgnoreCase(d.getHostname()));
                        boolean osUnknown = (d.getOperatingSystem() == null || "Unknown".equalsIgnoreCase(d.getOperatingSystem()));
                        boolean macUnknown = (d.getMacAddress() == null || d.getMacAddress().isEmpty());
                        boolean vendorUnknown = (d.getMacVendor() == null || d.getMacVendor().isEmpty());
                        if (hostnameUnknown || osUnknown || macUnknown || vendorUnknown) {
                            detailedScan(ip);
                        }
                    } else {
                        detailedScan(ip);
                    }
                } catch (Exception enrichErr) {
                    log.debug("Enrichment failed for {}: {}", ip, enrichErr.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Scan failed: {}", e.getMessage());
            result.setStatus("FAILED");
            result.setMessage(e.getMessage());
        }

        return result;
    }

    private String resolveViaNbtstat(String ip) {
        try {
            Process p = Runtime.getRuntime().exec(new String[] {"cmd.exe", "/c", "nbtstat -A " + ip});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("<00>") && line.contains("UNIQUE")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 0) {
                        return parts[0];
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String resolveViaPing(String ip) {
        try {
            Process p = Runtime.getRuntime().exec(new String[] {"cmd.exe", "/c", "ping -a -n 1 " + ip});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Pinging")) {
                    int start = line.indexOf("Pinging") + 8;
                    int bracket = line.indexOf(" [");
                    if (bracket > start) {
                        return line.substring(start, bracket).trim();
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public ScanResult detailedScan(String ipAddress) {
        ScanResult result = new ScanResult();
        result.setNetworkRange(ipAddress);
        result.setStatus("RUNNING");

        try {
            String command = "nmap -O -sV -R --script nbstat,smb-os-discovery -p 137,139,445 " + ipAddress;
            log.info("Executing detailed scan: {}", command);

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            String hostname = null;
            String mac = null;
            String macVendor = null;
            String os = null;

            while ((line = reader.readLine()) != null) {
                if (line.contains("MAC Address:")) {
                    String[] parts = line.split("MAC Address: ");
                    if (parts.length > 1) {
                        String[] macParts = parts[1].split(" ");
                        mac = macParts[0];
                        if (macParts.length > 1) {
                            macVendor = parts[1].substring(mac.length()).trim().replaceAll("[()]", "");
                        }
                    }
                }
                if (line.contains("OS details:")) {
                    os = line.split("OS details: ")[1];
                } else if (line.startsWith("Running:")) {
                    String running = line.replace("Running:", "").trim();
                    if (!running.isEmpty()) {
                        os = running;
                    }
                } else if (line.startsWith("OS guesses:")) {
                    String guesses = line.replace("OS guesses:", "").trim();
                    if (!guesses.isEmpty()) {
                        int comma = guesses.indexOf(',');
                        os = comma > 0 ? guesses.substring(0, comma).trim() : guesses;
                    }
                }
                if (line.contains("NetBIOS name:")) {
                    String[] parts = line.split("NetBIOS name:");
                    if (parts.length > 1) {
                        String nb = parts[1].trim();
                        int sp = nb.indexOf(' ');
                        hostname = sp > 0 ? nb.substring(0, sp).trim() : nb;
                    }
                }
                if (line.contains("Computer name:")) {
                    String[] parts = line.split("Computer name:");
                    if (parts.length > 1) {
                        String name = parts[1].trim();
                        int sp = name.indexOf(' ');
                        hostname = sp > 0 ? name.substring(0, sp).trim() : name;
                    }
                }
                if (line.contains("Nmap scan report for")) {
                    String afterFor = line.substring(line.indexOf("for") + 4).trim();
                    if (afterFor.contains("(") && afterFor.contains(")")) {
                        String possibleName = afterFor.substring(0, afterFor.indexOf("(")).trim();
                        if (!possibleName.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                            hostname = possibleName;
                        }
                    } else if (!afterFor.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                        hostname = afterFor.split(" ")[0];
                    }
                }
            }

            process.waitFor();
            deviceService.createOrUpdateDevice(ipAddress, mac, hostname, os, macVendor);

            result.getDiscoveredIps().add(ipAddress);
            result.setDevicesFound(1);
            result.setStatus("COMPLETED");
            result.setMessage("Detailed scan completed");
            log.info("Detailed scan completed for {}", ipAddress);

        } catch (Exception e) {
            log.error("Detailed scan failed: {}", e.getMessage());
            result.setStatus("FAILED");
            result.setMessage(e.getMessage());
        }

        return result;
    }
}
