package com.luontd.resumeservice.infrastructure.persistence;

import com.luontd.resumeservice.domain.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IResumeSkillJpaRepository extends JpaRepository<ResumeSkill, UUID> {

    @Modifying
    @Query("DELETE FROM ResumeSkill s WHERE s.resume.id = :resumeId")
    void deleteAllByResumeId(@Param("resumeId") UUID resumeId);

    List<ResumeSkill> findByResumeIdIn(List<UUID> resumeIds);
}
