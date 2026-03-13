package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class DeviceControlService {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AuditLogService auditLogService;

    public String disconnectDevice(String ipAddress) {
        try {
            Device device = deviceService.getDeviceByIp(ipAddress).orElse(null);
            if (device == null) {
                return "Device not found";
            }

            // Block device using Windows Firewall
            String command = String.format(
                "netsh advfirewall firewall add rule name=\"Block_%s\" dir=in action=block remoteip=%s",
                ipAddress.replace(".", "_"), ipAddress
            );

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                device.setStatus(Device.DeviceStatus.OFFLINE);
                deviceService.updateDevice(device.getId(), device);
                auditLogService.log("system", "DISCONNECT_DEVICE", ipAddress, "Device blocked via firewall", null);
                return "Device " + ipAddress + " blocked successfully";
            } else {
                return "Failed to block device. Run as administrator.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String reconnectDevice(String ipAddress) {
        try {
            Device device = deviceService.getDeviceByIp(ipAddress).orElse(null);
            if (device == null) {
                return "Device not found";
            }

            String command = String.format(
                "netsh advfirewall firewall delete rule name=\"Block_%s\"",
                ipAddress.replace(".", "_")
            );

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                device.setStatus(Device.DeviceStatus.ONLINE);
                deviceService.updateDevice(device.getId(), device);
                auditLogService.log("system", "RECONNECT_DEVICE", ipAddress, "Device unblocked via firewall", null);
                return "Device " + ipAddress + " unblocked successfully";
            } else {
                return "Failed to unblock device";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getBlockedDevices() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", 
                "netsh advfirewall firewall show rule name=all | findstr Block_");
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
