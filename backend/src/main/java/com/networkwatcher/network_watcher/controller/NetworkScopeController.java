package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.model.NetworkScope;
import com.networkwatcher.network_watcher.service.NetworkScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/networks")
@CrossOrigin(origins = "*")
public class NetworkScopeController {

    @Autowired
    private NetworkScopeService service;

    @GetMapping
    public List<NetworkScope> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NetworkScope> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public NetworkScope create(@RequestBody NetworkScope scope) {
        return service.create(scope);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<NetworkScope> update(@PathVariable Long id, @RequestBody NetworkScope scope) {
        return service.update(id, scope)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
