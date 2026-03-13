package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.NetworkScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class ScheduledScanService {

    @Autowired
    private NetworkScopeService networkScopeService;

    @Autowired
    private NetworkScanService networkScanService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private WebSocketNotificationService wsService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledNetworkScan() {
        log.info("Starting scheduled network scan at 2 AM");
        
        List<NetworkScope> enabledNetworks = networkScopeService.getEnabled();
        
        for (NetworkScope network : enabledNetworks) {
            try {
                log.info("Scanning network: {} ({})", network.getName(), network.getCidr());
                networkScanService.scanNetwork(network.getCidr());
            } catch (Exception e) {
                log.error("Failed to scan network {}: {}", network.getName(), e.getMessage());
            }
        }
        
        log.info("Scheduled network scan completed");
    }

    @Scheduled(fixedRate = 300000)
    public void periodicDeviceCheck() {
        log.debug("Periodic device status check (every 5 minutes)");
        var devices = deviceService.getAllDevices();
        for (var d : devices) {
            try {
                boolean reachable = java.net.InetAddress.getByName(d.getIpAddress()).isReachable(1500);
                var newStatus = reachable ? com.networkwatcher.network_watcher.model.Device.DeviceStatus.ONLINE
                                          : com.networkwatcher.network_watcher.model.Device.DeviceStatus.OFFLINE;
                if (d.getStatus() != newStatus) {
                    d.setStatus(newStatus);
                    d.setLastSeen(java.time.LocalDateTime.now());
                    deviceService.saveDevice(d);
                    wsService.notifyDeviceStatus(d.getIpAddress(), newStatus.toString());

                    if (newStatus == com.networkwatcher.network_watcher.model.Device.DeviceStatus.OFFLINE) {
                        var alert = new com.networkwatcher.network_watcher.model.Alert();
                        alert.setTitle("Device DOWN");
                        alert.setDescription("Device " + d.getIpAddress() + " is OFFLINE");
                        alert.setSeverity(com.networkwatcher.network_watcher.model.Alert.AlertSeverity.HIGH);
                        alert.setStatus(com.networkwatcher.network_watcher.model.Alert.AlertStatus.OPEN);
                        alert.setType(com.networkwatcher.network_watcher.model.Alert.AlertType.DEVICE_OFFLINE);
                        alert.setDevice(d);
                        alertService.create(alert);
                    }
                }
            } catch (Exception e) {
                log.debug("Ping failed for {}: {}", d.getIpAddress(), e.getMessage());
            }
        }
    }
}
