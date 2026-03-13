package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.BandwidthUsage;
import com.networkwatcher.network_watcher.model.Device;
import com.networkwatcher.network_watcher.repository.BandwidthRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BandwidthService {

    @Autowired
    private BandwidthRepository bandwidthRepository;

    @Autowired
    private DeviceService deviceService;

    public BandwidthUsage recordBandwidthForDevice(String ipAddress) {
        try {
            Device device = deviceService.getDeviceByIp(ipAddress)
                    .orElseThrow(() -> new RuntimeException("Device not found"));

            Map<String, Long> stats = getNetworkStats(ipAddress);

            BandwidthUsage usage = new BandwidthUsage();
            usage.setDevice(device);
            usage.setBytesSent(stats.getOrDefault("bytesSent", 0L));
            usage.setBytesReceived(stats.getOrDefault("bytesReceived", 0L));
            usage.setPacketsSent(stats.getOrDefault("packetsSent", 0L));
            usage.setPacketsReceived(stats.getOrDefault("packetsReceived", 0L));

            return bandwidthRepository.save(usage);

        } catch (Exception e) {
            log.error("Failed to record bandwidth: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Long> getNetworkStats(String ipAddress) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("bytesSent", 0L);
        stats.put("bytesReceived", 0L);
        stats.put("packetsSent", 0L);
        stats.put("packetsReceived", 0L);

        try {
            String command = "netstat -e";
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//These patterns extract numbers from text output.
            String line;
            Pattern bytesPattern = Pattern.compile("Bytes\\s+(\\d+)\\s+(\\d+)");
            Pattern packetsPattern = Pattern.compile("Unicast packets\\s+(\\d+)\\s+(\\d+)");

            while ((line = reader.readLine()) != null) {
                Matcher bytesMatcher = bytesPattern.matcher(line);
                if (bytesMatcher.find()) {
                    stats.put("bytesReceived", Long.parseLong(bytesMatcher.group(1)));
                    stats.put("bytesSent", Long.parseLong(bytesMatcher.group(2)));
                }

                Matcher packetsMatcher = packetsPattern.matcher(line);
                if (packetsMatcher.find()) {
                    stats.put("packetsReceived", Long.parseLong(packetsMatcher.group(1)));
                    stats.put("packetsSent", Long.parseLong(packetsMatcher.group(2)));
                }
            }

            process.waitFor();

        } catch (Exception e) {
            log.error("Failed to get network stats: {}", e.getMessage());
        }

        return stats;
    }

    public List<BandwidthUsage> getDeviceBandwidth(Long deviceId) {
        return bandwidthRepository.findByDeviceId(deviceId);
    }

    public List<BandwidthUsage> getAllBandwidthRecords() {
        return bandwidthRepository.findAll();
    }

    public Map<String, Object> getBandwidthSummary(Long deviceId) {
        List<BandwidthUsage> records = bandwidthRepository.findLatestByDeviceId(deviceId);
        
        Map<String, Object> summary = new HashMap<>();
        if (!records.isEmpty()) {
            long totalSent = records.stream().mapToLong(BandwidthUsage::getBytesSent).sum();
            long totalReceived = records.stream().mapToLong(BandwidthUsage::getBytesReceived).sum();
            
            summary.put("totalBytesSent", totalSent);
            summary.put("totalBytesReceived", totalReceived);
            summary.put("totalBytes", totalSent + totalReceived);
            summary.put("totalMB", (totalSent + totalReceived) / (1024.0 * 1024.0));
            summary.put("recordCount", records.size());
        }
        
        return summary;
    }
}
