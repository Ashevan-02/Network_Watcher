package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.model.ScanJob;
import com.networkwatcher.network_watcher.service.ScanJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/scans")
public class ScanJobController {

    @Autowired
    private ScanJobService service;

    @GetMapping
    public List<ScanJob> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanJob> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ScanJob create(@RequestBody ScanJob job) {
        return service.create(job);
    }

    @PutMapping("/{id}/status")
    public ScanJob updateStatus(@PathVariable Long id, @RequestParam ScanJob.ScanStatus status) {
        return service.updateStatus(id, status);
    }

    @PostMapping("/start")
    public ResponseEntity<ScanJob> startScan(@RequestBody ScanJob job) {
        ScanJob created = service.create(job);
        service.updateStatus(created.getId(), ScanJob.ScanStatus.RUNNING);
        return ResponseEntity.ok(created);
    }
}
