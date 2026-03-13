package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.AuditLog;
import com.networkwatcher.network_watcher.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository repository;

    public void log(String username, String action, String resource, String details, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setResource(resource);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        repository.save(log);
    }
}
