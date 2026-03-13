package com.networkwatcher.network_watcher.repository;

import com.networkwatcher.network_watcher.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(Alert.AlertStatus status);
    List<Alert> findByDeviceId(Long deviceId);
    List<Alert> findBySeverity(Alert.AlertSeverity severity);
}
