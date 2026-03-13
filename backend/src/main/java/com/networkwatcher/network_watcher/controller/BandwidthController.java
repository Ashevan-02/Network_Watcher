package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.model.BandwidthUsage;
import com.networkwatcher.network_watcher.service.BandwidthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bandwidth")
@Slf4j
public class BandwidthController {

    @Autowired
    private BandwidthService bandwidthService;

    @PostMapping("/record/{ip}")
    public ResponseEntity<BandwidthUsage> recordBandwidth(@PathVariable String ip) {
        log.info("POST /api/bandwidth/record/{} - Recording bandwidth", ip);
        BandwidthUsage usage = bandwidthService.recordBandwidthForDevice(ip);
        return ResponseEntity.ok(usage);
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<BandwidthUsage>> getDeviceBandwidth(@PathVariable Long deviceId) {
        log.info("GET /api/bandwidth/device/{} - Getting bandwidth history", deviceId);
        List<BandwidthUsage> records = bandwidthService.getDeviceBandwidth(deviceId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/device/{deviceId}/summary")
    public ResponseEntity<Map<String, Object>> getBandwidthSummary(@PathVariable Long deviceId) {
        log.info("GET /api/bandwidth/device/{}/summary - Getting bandwidth summary", deviceId);
        Map<String, Object> summary = bandwidthService.getBandwidthSummary(deviceId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    public ResponseEntity<List<BandwidthUsage>> getAllBandwidth() {
        log.info("GET /api/bandwidth - Getting all bandwidth records");
        List<BandwidthUsage> records = bandwidthService.getAllBandwidthRecords();
        return ResponseEntity.ok(records);
    }
}
