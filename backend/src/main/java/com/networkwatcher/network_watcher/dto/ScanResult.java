package com.networkwatcher.network_watcher.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScanResult {
    private String networkRange;
    private int devicesFound;
    private List<String> discoveredIps = new ArrayList<>();
    private String status;
    private String message;
}
