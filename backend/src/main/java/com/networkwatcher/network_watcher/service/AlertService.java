package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.Alert;
import com.networkwatcher.network_watcher.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    @Autowired
    private AlertRepository repository;

    public List<Alert> getAll() {
        return repository.findAll();
    }

    public Optional<Alert> getById(Long id) {
        return repository.findById(id);
    }

    public Alert create(Alert alert) {
        return repository.save(alert);
    }

    public Alert acknowledge(Long id, String username) {
        Alert alert = repository.findById(id).orElseThrow();
        alert.setStatus(Alert.AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(username);
        return repository.save(alert);
    }

    public Alert dismiss(Long id) {
        Alert alert = repository.findById(id).orElseThrow();
        alert.setStatus(Alert.AlertStatus.DISMISSED);
        return repository.save(alert);
    }

    public Alert escalate(Long id) {
        Alert alert = repository.findById(id).orElseThrow();
        alert.setStatus(Alert.AlertStatus.ESCALATED);
        return repository.save(alert);
    }

    public List<Alert> getByStatus(Alert.AlertStatus status) {
        return repository.findByStatus(status);
    }
}
