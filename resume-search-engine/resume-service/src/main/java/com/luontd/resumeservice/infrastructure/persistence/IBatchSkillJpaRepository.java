package com.luontd.resumeservice.infrastructure.persistence;

import com.luontd.resumeservice.domain.entity.BatchSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IBatchSkillJpaRepository extends JpaRepository<BatchSkill, UUID> {
    List<BatchSkill> findByBatchId(UUID batchId);
}
