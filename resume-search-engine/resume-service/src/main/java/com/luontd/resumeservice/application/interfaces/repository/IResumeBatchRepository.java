package com.luontd.resumeservice.application.interfaces.repository;

import com.luontd.resumeservice.domain.entity.ResumeBatch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface cho ResumeBatch repository.
 * Được định nghĩa ở Application layer — infrastructure phải implement.
 */
public interface IResumeBatchRepository {

    ResumeBatch save(ResumeBatch batch);

    Optional<ResumeBatch> findById(UUID id);

    List<ResumeBatch> findAll();

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
