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

            // Block device traffic to/from this host using Windows Firewall.
            // Note: this isolates the device only relative to the watcher host (not the whole LAN).
            String safeName = ipAddress.replace(".", "_");
            String commandIn = String.format(
                "netsh advfirewall firewall add rule name=\"BlockIn_%s\" dir=in action=block remoteip=%s",
                safeName, ipAddress
            );
            String commandOut = String.format(
                "netsh advfirewall firewall add rule name=\"BlockOut_%s\" dir=out action=block remoteip=%s",
                safeName, ipAddress
            );

            int exitIn = new ProcessBuilder("cmd.exe", "/c", commandIn).start().waitFor();
            int exitOut = new ProcessBuilder("cmd.exe", "/c", commandOut).start().waitFor();

            if (exitIn == 0 && exitOut == 0) {
                device.setStatus(Device.DeviceStatus.OFFLINE);
                if (device.getLeaveTime() == null) device.setLeaveTime(java.time.LocalDateTime.now());
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

            String safeName = ipAddress.replace(".", "_");
            String commandIn = String.format(
                "netsh advfirewall firewall delete rule name=\"BlockIn_%s\"",
                safeName
            );
            String commandOut = String.format(
                "netsh advfirewall firewall delete rule name=\"BlockOut_%s\"",
                safeName
            );

            int exitIn = new ProcessBuilder("cmd.exe", "/c", commandIn).start().waitFor();
            int exitOut = new ProcessBuilder("cmd.exe", "/c", commandOut).start().waitFor();

            if (exitIn == 0 && exitOut == 0) {
                device.setStatus(Device.DeviceStatus.ONLINE);
                device.setLeaveTime(null);
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
                "netsh advfirewall firewall show rule name=all | findstr BlockIn_ BlockOut_");
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
