package com.networkwatcher.network_watcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpPacketDetails {
    private String method;
    private String url;
    private String host;
    private String userAgent;
    private String contentType;
    private String sourceIp;
    private String destinationIp;
    private LocalDateTime timestamp;
}
