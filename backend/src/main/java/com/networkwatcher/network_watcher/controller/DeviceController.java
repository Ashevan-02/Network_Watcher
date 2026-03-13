package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.model.Device;
import com.networkwatcher.network_watcher.service.DeviceService;
import com.networkwatcher.network_watcher.service.DeviceControlService;
import com.networkwatcher.network_watcher.service.SnmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
@Slf4j
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceControlService deviceControlService;

    @Autowired
    private SnmpService snmpService;
    @GetMapping
    public List<Device> getAllDevices() {
        log.info("GET /api/devices - Fetching all devices");
        return deviceService.getAllDevices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        log.info("GET /api/devices/{} - Fetching device by ID", id);
        return deviceService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Device createDevice(@RequestBody Device device) {
        log.info("POST /api/devices - Creating new device");
        return deviceService.createDevice(device);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        log.info("PUT /api/devices/{} - Updating device", id);
        return deviceService.updateDevice(id, device)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.info("DELETE /api/devices/{} - Deleting device", id);
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ip/{ipAddress}")
    public ResponseEntity<Device> getDeviceByIp(@PathVariable String ipAddress) {
        log.info("GET /api/devices/ip/{} - Fetching device by IP", ipAddress);
        return deviceService.getDeviceByIp(ipAddress)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<Device> getDevicesByStatus(@PathVariable Device.DeviceStatus status) {
        log.info("GET /api/devices/status/{} - Fetching devices by status", status);
        return deviceService.getDevicesByStatus(status);
    }

    @GetMapping("/vulnerable")
    public List<Device> getVulnerableDevices() {
        log.info("GET /api/devices/vulnerable - Fetching vulnerable devices");
        return deviceService.getVulnerableDevices();
    }

    @PostMapping("/disconnect/{ipAddress}")
    public ResponseEntity<String> disconnectDevice(@PathVariable String ipAddress) {
        log.info("POST /api/devices/disconnect/{} - Disconnecting device", ipAddress);
        String result = deviceControlService.disconnectDevice(ipAddress);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reconnect/{ipAddress}")
    public ResponseEntity<String> reconnectDevice(@PathVariable String ipAddress) {
        log.info("POST /api/devices/reconnect/{} - Reconnecting device", ipAddress);
        String result = deviceControlService.reconnectDevice(ipAddress);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/blocked")
    public ResponseEntity<String> getBlockedDevices() {
        log.info("GET /api/devices/blocked - Fetching blocked devices");
        String result = deviceControlService.getBlockedDevices();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/snmp/{ipAddress}")
    public ResponseEntity<String> snmpEnrich(@PathVariable String ipAddress, @RequestParam(defaultValue = "public") String community) {
        boolean ok = snmpService.enrichDevice(ipAddress, community);
        return ResponseEntity.ok(ok ? "SNMP enrichment completed" : "SNMP enrichment failed or no data");
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllDevices() {
        log.info("DELETE /api/devices - Deleting all devices");
        deviceService.deleteAllDevices();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/range")
    public ResponseEntity<String> deleteByRange(@RequestParam String cidr) {
        log.info("DELETE /api/devices/range?cidr={} - Deleting devices in range", cidr);
        int count = deviceService.deleteDevicesByCidr(cidr);
        return ResponseEntity.ok("Deleted " + count + " devices in " + cidr);
    }
}
