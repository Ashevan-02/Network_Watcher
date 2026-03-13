package com.networkwatcher.network_watcher.repository;

import com.networkwatcher.network_watcher.model.BandwidthUsage;
import com.networkwatcher.network_watcher.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BandwidthRepository extends JpaRepository<BandwidthUsage, Long> {
    List<BandwidthUsage> findByDevice(Device device);
    List<BandwidthUsage> findByDeviceId(Long deviceId);
    List<BandwidthUsage> findByRecordedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT b FROM BandwidthUsage b WHERE b.device.id = ?1 ORDER BY b.recordedAt DESC")
    List<BandwidthUsage> findLatestByDeviceId(Long deviceId);
}
