package com.luontd.resumeservice.infrastructure.persistence.impl;

import com.luontd.resumeservice.application.interfaces.repository.IResumeBatchRepository;
import com.luontd.resumeservice.domain.entity.ResumeBatch;
import com.luontd.resumeservice.infrastructure.persistence.IResumeBatchJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation của IResumeBatchRepository.
 * Delegate sang Spring Data JPA repository.
 */
@Repository
@RequiredArgsConstructor
public class ResumeBatchRepositoryImpl implements IResumeBatchRepository {

    private final IResumeBatchJpaRepository jpaRepository;

    @Override
    public ResumeBatch save(ResumeBatch batch) {
        return jpaRepository.save(batch);
    }

    @Override
    public Optional<ResumeBatch> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ResumeBatch> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
