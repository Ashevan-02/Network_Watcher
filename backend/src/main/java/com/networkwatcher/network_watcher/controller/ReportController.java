package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.dto.NetworkReport;
import com.networkwatcher.network_watcher.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/json")
    public ResponseEntity<NetworkReport> getJsonReport() {
        log.info("GET /api/reports/json - Generating JSON report");
        NetworkReport report = reportService.generateReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/text")
    public ResponseEntity<String> getTextReport() {
        log.info("GET /api/reports/text - Generating text report");
        String report = reportService.generateTextReport();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "network-report.txt");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(report);
    }
}
