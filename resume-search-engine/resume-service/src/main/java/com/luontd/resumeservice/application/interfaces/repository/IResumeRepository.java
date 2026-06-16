package com.luontd.resumeservice.application.interfaces.repository;

import com.luontd.resumeservice.domain.entity.Resume;

import java.util.Optional;
import java.util.UUID;

/**
 * Port interface cho Resume repository.
 * Được định nghĩa ở Application layer — infrastructure phải implement.
 */
public interface IResumeRepository {

    Resume save(Resume resume);

    Optional<Resume> findById(UUID id);
}
