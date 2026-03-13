package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.SnmpCommunity;
import com.networkwatcher.network_watcher.repository.SnmpCommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SnmpSettingsService {
    @Autowired
    private SnmpCommunityRepository repository;

    public List<String> getCommunities() {
        return repository.findAll().stream().map(SnmpCommunity::getCommunity).collect(Collectors.toList());
    }

    public void addCommunity(String community) {
        repository.findByCommunity(community).orElseGet(() -> {
            SnmpCommunity sc = new SnmpCommunity();
            sc.setCommunity(community);
            return repository.save(sc);
        });
    }

    public void removeCommunity(String community) {
        repository.deleteByCommunity(community);
    }
}
