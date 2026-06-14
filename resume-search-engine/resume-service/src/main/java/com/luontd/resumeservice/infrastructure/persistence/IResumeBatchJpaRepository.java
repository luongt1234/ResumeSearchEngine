package com.luontd.resumeservice.infrastructure.persistence;

import com.luontd.resumeservice.domain.entity.ResumeBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IResumeBatchJpaRepository extends JpaRepository<ResumeBatch, UUID> {
    Optional<ResumeBatch> findById(UUID uuid);
}
