package com.networkwatcher.network_watcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkReport {
    private LocalDateTime generatedAt;
    private int totalDevices;
    private int onlineDevices;
    private int offlineDevices;
    private int vulnerableDevices;
    private long totalBandwidthMB;
    private List<DeviceSummary> devices;
    private Map<String, Integer> devicesByOS;
    private Map<String, Integer> devicesByVendor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceSummary {
        private String ipAddress;
        private String hostname;
        private String macAddress;
        private String macVendor;
        private String operatingSystem;
        private String status;
        private boolean vulnerable;
        private LocalDateTime firstSeen;
        private LocalDateTime lastSeen;
    }
}
