package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.ScanJob;
import com.networkwatcher.network_watcher.repository.ScanJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScanJobService {

    @Autowired
    private ScanJobRepository repository;

    public List<ScanJob> getAll() {
        return repository.findAll();
    }

    public Optional<ScanJob> getById(Long id) {
        return repository.findById(id);
    }

    public ScanJob create(ScanJob job) {
        return repository.save(job);
    }

    public ScanJob updateStatus(Long id, ScanJob.ScanStatus status) {
        ScanJob job = repository.findById(id).orElseThrow();
        job.setStatus(status);
        if (status == ScanJob.ScanStatus.RUNNING) {
            job.setStartedAt(LocalDateTime.now());
        } else if (status == ScanJob.ScanStatus.SUCCESS || status == ScanJob.ScanStatus.FAILED) {
            job.setCompletedAt(LocalDateTime.now());
        }
        return repository.save(job);
    }

    public List<ScanJob> getByStatus(ScanJob.ScanStatus status) {
        return repository.findByStatus(status);
    }
}
