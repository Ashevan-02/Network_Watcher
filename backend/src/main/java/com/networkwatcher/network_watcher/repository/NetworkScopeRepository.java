package com.networkwatcher.network_watcher.repository;

import com.networkwatcher.network_watcher.model.NetworkScope;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NetworkScopeRepository extends JpaRepository<NetworkScope, Long> {
    List<NetworkScope> findByEnabled(Boolean enabled);
}
