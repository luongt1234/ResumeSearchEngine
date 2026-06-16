package com.luontd.resumeservice.infrastructure.persistence.impl;

import com.luontd.resumeservice.application.interfaces.repository.IBatchSkillRepository;
import com.luontd.resumeservice.domain.entity.BatchSkill;
import com.luontd.resumeservice.infrastructure.persistence.IBatchSkillJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation của IBatchSkillRepository.
 * Delegate sang Spring Data JPA repository.
 */
@Repository
@RequiredArgsConstructor
public class BatchSkillRepositoryImpl implements IBatchSkillRepository {

    private final IBatchSkillJpaRepository jpaRepository;

    @Override
    public BatchSkill save(BatchSkill skill) {
        return jpaRepository.save(skill);
    }

    @Override
    public Optional<BatchSkill> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<BatchSkill> findByBatchId(UUID batchId) {
        return jpaRepository.findByBatchId(batchId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
