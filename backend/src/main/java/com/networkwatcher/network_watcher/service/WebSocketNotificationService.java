package com.networkwatcher.network_watcher.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyDeviceDiscovered(String ipAddress) {
        messagingTemplate.convertAndSend("/topic/devices", 
            new NotificationMessage("DEVICE_DISCOVERED", ipAddress));
    }

    public void notifyVulnerabilityFound(String ipAddress, String vulnerability) {
        messagingTemplate.convertAndSend("/topic/vulnerabilities", 
            new NotificationMessage("VULNERABILITY_FOUND", ipAddress + ": " + vulnerability));
    }

    public void notifyScanComplete(int devicesFound) {
        messagingTemplate.convertAndSend("/topic/scans", 
            new NotificationMessage("SCAN_COMPLETE", "Found " + devicesFound + " devices"));
    }

    public void notifyDeviceStatus(String ipAddress, String status) {
        messagingTemplate.convertAndSend("/topic/devices", 
            new NotificationMessage("DEVICE_STATUS", ipAddress + " " + status));
    }
    public static class NotificationMessage {
        private String type;
        private String message;
        private long timestamp;

        public NotificationMessage(String type, String message) {
            this.type = type;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getType() { return type; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}
