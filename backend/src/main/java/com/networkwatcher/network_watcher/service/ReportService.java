package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.dto.NetworkReport;
import com.networkwatcher.network_watcher.model.BandwidthUsage;
import com.networkwatcher.network_watcher.model.Device;
import com.networkwatcher.network_watcher.repository.BandwidthRepository;
import com.networkwatcher.network_watcher.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private BandwidthRepository bandwidthRepository;

    public NetworkReport generateReport() {
        List<Device> allDevices = deviceRepository.findAll();
        
        int totalDevices = allDevices.size();
        int onlineDevices = (int) allDevices.stream()
            .filter(d -> d.getStatus() == Device.DeviceStatus.ONLINE).count();
        int offlineDevices = (int) allDevices.stream()
            .filter(d -> d.getStatus() == Device.DeviceStatus.OFFLINE).count();
        int vulnerableDevices = (int) allDevices.stream()
            .filter(Device::getIsVulnerable).count();

        List<BandwidthUsage> allBandwidth = bandwidthRepository.findAll();
        long totalBandwidthBytes = allBandwidth.stream()
            .mapToLong(BandwidthUsage::getTotalBytes).sum();
        long totalBandwidthMB = totalBandwidthBytes / (1024 * 1024);

        List<NetworkReport.DeviceSummary> deviceSummaries = allDevices.stream()
            .map(d -> new NetworkReport.DeviceSummary(
                d.getIpAddress(),
                d.getHostname(),
                d.getMacAddress(),
                d.getMacVendor(),
                d.getOperatingSystem(),
                d.getStatus().toString(),
                d.getIsVulnerable(),
                d.getFirstSeen(),
                d.getLastSeen()
            ))
            .collect(Collectors.toList());

        Map<String, Integer> devicesByOS = allDevices.stream()
            .filter(d -> d.getOperatingSystem() != null)
            .collect(Collectors.groupingBy(
                Device::getOperatingSystem,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));

        Map<String, Integer> devicesByVendor = allDevices.stream()
            .filter(d -> d.getMacVendor() != null)
            .collect(Collectors.groupingBy(
                Device::getMacVendor,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));

        return new NetworkReport(
            LocalDateTime.now(),
            totalDevices,
            onlineDevices,
            offlineDevices,
            vulnerableDevices,
            totalBandwidthMB,
            deviceSummaries,
            devicesByOS,
            devicesByVendor
        );
    }

    public String generateTextReport() {
        NetworkReport report = generateReport();
        StringBuilder sb = new StringBuilder();
        
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("           NETWORK WATCHER - SECURITY REPORT\n");
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("Generated: ").append(report.getGeneratedAt()).append("\n\n");
        
        sb.append("SUMMARY:\n");
        sb.append("  Total Devices: ").append(report.getTotalDevices()).append("\n");
        sb.append("  Online: ").append(report.getOnlineDevices()).append("\n");
        sb.append("  Offline: ").append(report.getOfflineDevices()).append("\n");
        sb.append("  Vulnerable: ").append(report.getVulnerableDevices()).append("\n");
        sb.append("  Total Bandwidth: ").append(report.getTotalBandwidthMB()).append(" MB\n\n");
        
        sb.append("DEVICES BY OS:\n");
        report.getDevicesByOS().forEach((os, count) -> 
            sb.append("  ").append(os).append(": ").append(count).append("\n"));
        
        sb.append("\nDEVICES BY VENDOR:\n");
        report.getDevicesByVendor().forEach((vendor, count) -> 
            sb.append("  ").append(vendor).append(": ").append(count).append("\n"));
        
        sb.append("\n═══════════════════════════════════════════════════════════\n");
        sb.append("DEVICE DETAILS:\n");
        sb.append("═══════════════════════════════════════════════════════════\n");
        
        for (NetworkReport.DeviceSummary device : report.getDevices()) {
            sb.append("\nIP: ").append(device.getIpAddress()).append("\n");
            sb.append("  Hostname: ").append(device.getHostname() != null ? device.getHostname() : "N/A").append("\n");
            sb.append("  MAC: ").append(device.getMacAddress() != null ? device.getMacAddress() : "N/A").append("\n");
            sb.append("  Vendor: ").append(device.getMacVendor() != null ? device.getMacVendor() : "N/A").append("\n");
            sb.append("  OS: ").append(device.getOperatingSystem() != null ? device.getOperatingSystem() : "N/A").append("\n");
            sb.append("  Status: ").append(device.getStatus()).append("\n");
            sb.append("  Vulnerable: ").append(device.isVulnerable() ? "YES ⚠️" : "NO").append("\n");
            sb.append("  First Seen: ").append(device.getFirstSeen()).append("\n");
            sb.append("  Last Seen: ").append(device.getLastSeen()).append("\n");
        }
        
        return sb.toString();
    }
}
