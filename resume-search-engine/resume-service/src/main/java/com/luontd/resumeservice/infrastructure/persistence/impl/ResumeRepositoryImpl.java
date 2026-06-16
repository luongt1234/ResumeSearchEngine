package com.luontd.resumeservice.infrastructure.persistence.impl;

import com.luontd.resumeservice.application.interfaces.repository.IResumeRepository;
import com.luontd.resumeservice.domain.entity.Resume;
import com.luontd.resumeservice.infrastructure.persistence.IResumeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation của IResumeRepository.
 * Delegate sang Spring Data JPA repository.
 */
@Repository
@RequiredArgsConstructor
public class ResumeRepositoryImpl implements IResumeRepository {

    private final IResumeJpaRepository jpaRepository;

    @Override
    public Resume save(Resume resume) {
        return jpaRepository.save(resume);
    }

    @Override
    public Optional<Resume> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}
