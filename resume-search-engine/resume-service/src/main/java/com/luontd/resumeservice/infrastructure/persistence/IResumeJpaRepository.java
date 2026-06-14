package com.luontd.resumeservice.infrastructure.persistence;

import com.luontd.resumeservice.domain.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IResumeJpaRepository extends JpaRepository<Resume, UUID> {
}
