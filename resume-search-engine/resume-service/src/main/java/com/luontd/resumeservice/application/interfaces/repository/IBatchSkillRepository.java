package com.luontd.resumeservice.application.interfaces.repository;

import com.luontd.resumeservice.domain.entity.BatchSkill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface cho BatchSkill repository.
 * Được định nghĩa ở Application layer — infrastructure phải implement.
 */
public interface IBatchSkillRepository {

    BatchSkill save(BatchSkill skill);

    Optional<BatchSkill> findById(UUID id);

    List<BatchSkill> findByBatchId(UUID batchId);

    void deleteById(UUID id);
}
