package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.model.Alert;
import com.networkwatcher.network_watcher.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertService service;

    @GetMapping
    public List<Alert> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public Alert create(@RequestBody Alert alert) {
        return service.create(alert);
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'OPERATOR')")
    public Alert acknowledge(@PathVariable Long id, Authentication auth) {
        return service.acknowledge(id, auth.getName());
    }

    @PostMapping("/{id}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public Alert dismiss(@PathVariable Long id) {
        return service.dismiss(id);
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public Alert escalate(@PathVariable Long id) {
        return service.escalate(id);
    }
}
