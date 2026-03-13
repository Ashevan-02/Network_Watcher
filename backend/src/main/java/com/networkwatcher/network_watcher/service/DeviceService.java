package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.Device;
import com.networkwatcher.network_watcher.repository.DeviceRepository;
import com.networkwatcher.network_watcher.repository.BandwidthRepository;
import com.networkwatcher.network_watcher.repository.VulnerabilityRepository;
import com.networkwatcher.network_watcher.repository.AlertRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private BandwidthRepository bandwidthRepository;
    
    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    @Autowired
    private AlertRepository alertRepository;

    public List<Device> getAllDevices() {
        log.info("Fetching all devices");
        return deviceRepository.findAll();
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public Optional<Device> getDeviceByIp(String ipAddress) {
        return deviceRepository.findByIpAddress(ipAddress);
    }

    public List<Device> getOnlineDevices() {
        return deviceRepository.findByStatus(Device.DeviceStatus.ONLINE);
    }

    public List<Device> getVulnerableDevices() {
        return deviceRepository.findByIsVulnerableTrue();
    }

    public Device saveDevice(Device device) {
        if (device.getId() == null) {
            log.info("Creating new device: {}", device.getIpAddress());
        } else {
            log.info("Updating device: {}", device.getIpAddress());
        }
        
        return deviceRepository.save(device);
    }

    public Device createOrUpdateDevice(String ipAddress, String macAddress, 
                                       String hostname, String os) {
        
        Optional<Device> existingDevice = deviceRepository.findByIpAddress(ipAddress);
        
        Device device;
        if (existingDevice.isPresent()) {
            device = existingDevice.get();
            log.info("Updating existing device: {}", ipAddress);
        } else {
            device = new Device();
            device.setIpAddress(ipAddress);
            log.info("Creating new device: {}", ipAddress);
        }
        
        device.setMacAddress(macAddress);
        device.setHostname(hostname);
        device.setOperatingSystem(os);
        device.setStatus(Device.DeviceStatus.ONLINE);
        device.setLastSeen(LocalDateTime.now());
        
        return deviceRepository.save(device);
    }

    public Device createOrUpdateDevice(String ipAddress, String macAddress, 
                                       String hostname, String os, String macVendor) {
        
        Optional<Device> existingDevice = deviceRepository.findByIpAddress(ipAddress);
        
        Device device;
        if (existingDevice.isPresent()) {
            device = existingDevice.get();
            log.info("Updating existing device: {}", ipAddress);
        } else {
            device = new Device();
            device.setIpAddress(ipAddress);
            log.info("Creating new device: {}", ipAddress);
        }
        
        device.setMacAddress(macAddress);
        device.setMacVendor(macVendor);
        device.setHostname(hostname);
        device.setOperatingSystem(os);
        device.setStatus(Device.DeviceStatus.ONLINE);
        device.setLastSeen(LocalDateTime.now());
        
        return deviceRepository.save(device);
    }

    public void deleteDevice(Long id) {
        log.info("Deleting device with ID: {}", id);
        deviceRepository.deleteById(id);
    }

    public void deleteAllDevices() {
        log.info("Deleting all devices with related data");
        var allDevices = deviceRepository.findAll();
        for (Device d : allDevices) {
            alertRepository.findByDeviceId(d.getId()).forEach(alertRepository::delete);
            bandwidthRepository.findByDeviceId(d.getId()).forEach(bandwidthRepository::delete);
            vulnerabilityRepository.findByDeviceId(d.getId()).forEach(vulnerabilityRepository::delete);
            deviceRepository.delete(d);
        }
    }

    public int deleteDevicesByCidr(String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) return 0;
        String baseIp = parts[0];
        int prefix = Integer.parseInt(parts[1]);
        int base = ipv4ToInt(baseIp);
        int mask = prefix == 0 ? 0 : -1 << (32 - prefix);
        int network = base & mask;
        List<Device> all = deviceRepository.findAll();
        int count = 0;
        for (Device d : all) {
            if (d.getIpAddress() == null) continue;
            int ip = ipv4ToInt(d.getIpAddress());
            if ((ip & mask) == network) {
                alertRepository.findByDeviceId(d.getId()).forEach(alertRepository::delete);
                bandwidthRepository.findByDeviceId(d.getId()).forEach(bandwidthRepository::delete);
                vulnerabilityRepository.findByDeviceId(d.getId()).forEach(vulnerabilityRepository::delete);
                deviceRepository.delete(d);
                count++;
            }
        }
        log.info("Deleted {} devices in {}", count, cidr);
        return count;
    }

    private int ipv4ToInt(String ip) {
        String[] oct = ip.split("\\.");
        if (oct.length != 4) return 0;
        int a = Integer.parseInt(oct[0]);
        int b = Integer.parseInt(oct[1]);
        int c = Integer.parseInt(oct[2]);
        int d = Integer.parseInt(oct[3]);
        return ((a & 0xFF) << 24) | ((b & 0xFF) << 16) | ((c & 0xFF) << 8) | (d & 0xFF);
    }

    public void markDeviceOffline(Long id) {
        Optional<Device> deviceOpt = deviceRepository.findById(id);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setStatus(Device.DeviceStatus.OFFLINE);
            deviceRepository.save(device);
            log.info("Marked device {} as OFFLINE", device.getIpAddress());
        }
    }

    public List<Device> searchByHostname(String hostname) {
        return deviceRepository.findByHostnameContainingIgnoreCase(hostname);
    }

    public long getDeviceCount() {
        return deviceRepository.count();
    }

    public long getOnlineDeviceCount() {
        return deviceRepository.findByStatus(Device.DeviceStatus.ONLINE).size();
    }

    public Device createDevice(Device device) {
        log.info("Creating new device: {}", device.getIpAddress());
        return deviceRepository.save(device);
    }

    public Optional<Device> updateDevice(Long id, Device updatedDevice) {
        return deviceRepository.findById(id).map(device -> {
            device.setIpAddress(updatedDevice.getIpAddress());
            device.setMacAddress(updatedDevice.getMacAddress());
            device.setHostname(updatedDevice.getHostname());
            device.setOperatingSystem(updatedDevice.getOperatingSystem());
            device.setStatus(updatedDevice.getStatus());
            device.setIsVulnerable(updatedDevice.getIsVulnerable());
            device.setLastSeen(LocalDateTime.now());
            log.info("Updated device: {}", device.getIpAddress());
            return deviceRepository.save(device);
        });
    }

    public List<Device> getDevicesByStatus(Device.DeviceStatus status) {
        return deviceRepository.findByStatus(status);
    }
}
