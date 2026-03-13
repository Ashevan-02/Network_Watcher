package com.networkwatcher.network_watcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${notification.email.from:noreply@networkwatcher.com}")
    private String fromEmail;

    @Value("${notification.email.to:admin@networkwatcher.com}")
    private String toEmail;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    public void sendVulnerabilityAlert(String deviceIp, String vulnerabilityName, String severity) {
        if (!emailEnabled || mailSender == null) {
            log.debug("Email notifications disabled or not configured");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⚠️ Vulnerability Alert: " + severity);
            message.setText(String.format(
                "Vulnerability Detected!\n\n" +
                "Device: %s\n" +
                "Vulnerability: %s\n" +
                "Severity: %s\n\n" +
                "Please investigate immediately.\n\n" +
                "Network Watcher System",
                deviceIp, vulnerabilityName, severity
            ));

            mailSender.send(message);
            log.info("Vulnerability alert email sent for device {}", deviceIp);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    public void sendDeviceOfflineAlert(String deviceIp, String hostname) {
        if (!emailEnabled || mailSender == null) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Device Offline: " + (hostname != null ? hostname : deviceIp));
            message.setText(String.format(
                "Device Offline Alert\n\n" +
                "Device: %s\n" +
                "Hostname: %s\n\n" +
                "The device is no longer responding to network scans.\n\n" +
                "Network Watcher System",
                deviceIp, hostname != null ? hostname : "Unknown"
            ));

            mailSender.send(message);
            log.info("Device offline alert sent for {}", deviceIp);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    public void sendScanCompletedReport(int devicesFound, int vulnerableDevices) {
        if (!emailEnabled || mailSender == null) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Network Scan Completed");
            message.setText(String.format(
                "Network Scan Summary\n\n" +
                "Total Devices Found: %d\n" +
                "Vulnerable Devices: %d\n\n" +
                "View full report at: http://localhost:8080/api/reports/json\n\n" +
                "Network Watcher System",
                devicesFound, vulnerableDevices
            ));

            mailSender.send(message);
            log.info("Scan completed report sent");
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }
}
