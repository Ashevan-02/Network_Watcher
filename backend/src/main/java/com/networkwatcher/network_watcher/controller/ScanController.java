package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.dto.ScanResult;
import com.networkwatcher.network_watcher.service.NetworkScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scan")
@Slf4j
public class ScanController {

    @Autowired
    private NetworkScanService scanService;

    @PostMapping("/network")
    public ResponseEntity<ScanResult> scanNetwork(@RequestParam String range) {
        log.info("POST /api/scan/network - Scanning network: {}", range);
        ScanResult result = scanService.scanNetwork(range);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/device/{ip}")
    public ResponseEntity<ScanResult> scanDevice(@PathVariable String ip) {
        log.info("POST /api/scan/device/{} - Detailed scan", ip);
        ScanResult result = scanService.detailedScan(ip);
        return ResponseEntity.ok(result);
    }
}
