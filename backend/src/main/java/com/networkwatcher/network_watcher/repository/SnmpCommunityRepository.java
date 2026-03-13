package com.networkwatcher.network_watcher.repository;

import com.networkwatcher.network_watcher.model.SnmpCommunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SnmpCommunityRepository extends JpaRepository<SnmpCommunity, Long> {
    Optional<SnmpCommunity> findByCommunity(String community);
    void deleteByCommunity(String community);
}
