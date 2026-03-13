package com.networkwatcher.network_watcher.repository;

import com.networkwatcher.network_watcher.model.ScanJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScanJobRepository extends JpaRepository<ScanJob, Long> {
    List<ScanJob> findByStatus(ScanJob.ScanStatus status);
    List<ScanJob> findByNetworkScopeId(Long networkScopeId);
}
