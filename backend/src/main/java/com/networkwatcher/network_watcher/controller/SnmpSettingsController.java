package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.service.SnmpSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snmp/communities")
public class SnmpSettingsController {

    @Autowired
    private SnmpSettingsService service;

    @GetMapping
    public List<String> getAll() {
        return service.getCommunities();
    }

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody Map<String, String> body) {
        String community = body.get("community");
        if (community == null || community.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        service.addCommunity(community.trim());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{community}")
    public ResponseEntity<Void> remove(@PathVariable String community) {
        service.removeCommunity(community);
        return ResponseEntity.noContent().build();
    }
}
