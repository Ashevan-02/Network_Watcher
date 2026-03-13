package com.networkwatcher.network_watcher.controller;

import com.networkwatcher.network_watcher.model.CapturedPacket;
import com.networkwatcher.network_watcher.service.PacketService;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packets")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PacketController {

    private final PacketService packetService;

    @Autowired
    public PacketController(PacketService packetService) {
        this.packetService = packetService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startCapturing(@RequestParam String interfaceIp) {
        try {
            packetService.startCapturing(interfaceIp);
            return ResponseEntity.ok("Packet capturing started on " + interfaceIp);
        } catch (PcapNativeException | NotOpenException e) {
            return ResponseEntity.internalServerError().body("Error starting capture: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopCapturing() {
        packetService.stopCapturing();
        return ResponseEntity.ok("Packet capturing stopped");
    }

    @GetMapping
    public ResponseEntity<List<CapturedPacket>> getPackets() {
        return ResponseEntity.ok(packetService.getCapturedPackets());
    }

    @GetMapping("/http")
    public ResponseEntity<?> getHttpPackets() {
        return ResponseEntity.ok(packetService.getHttpPackets());
    }

    @GetMapping("/dns")
    public ResponseEntity<?> getDnsPackets() {
        return ResponseEntity.ok(packetService.getDnsQueries());
    }

    @GetMapping("/apps")
    public ResponseEntity<Map<String, Integer>> getApps() {
        return ResponseEntity.ok(packetService.getAppSummary());
    }

    @DeleteMapping
    public ResponseEntity<String> clearPackets() {
        packetService.clearPackets();
        return ResponseEntity.ok("Packet history cleared");
    }
}
