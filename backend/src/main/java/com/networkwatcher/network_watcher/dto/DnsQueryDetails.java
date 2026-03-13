package com.networkwatcher.network_watcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DnsQueryDetails {
    private String domain;
    private String sourceIp;
    private LocalDateTime timestamp;
}
