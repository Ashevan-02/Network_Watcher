package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.NetworkScope;
import com.networkwatcher.network_watcher.repository.NetworkScopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NetworkScopeService {

    @Autowired
    private NetworkScopeRepository repository;

    public List<NetworkScope> getAll() {
        return repository.findAll();
    }

    public Optional<NetworkScope> getById(Long id) {
        return repository.findById(id);
    }

    public NetworkScope create(NetworkScope scope) {
        return repository.save(scope);
    }

    public Optional<NetworkScope> update(Long id, NetworkScope scope) {
        return repository.findById(id).map(existing -> {
            existing.setName(scope.getName());
            existing.setCidr(scope.getCidr());
            existing.setExclusions(scope.getExclusions());
            existing.setEnabled(scope.getEnabled());
            existing.setScanSchedule(scope.getScanSchedule());
            return repository.save(existing);
        });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<NetworkScope> getEnabled() {
        return repository.findByEnabled(true);
    }
}
