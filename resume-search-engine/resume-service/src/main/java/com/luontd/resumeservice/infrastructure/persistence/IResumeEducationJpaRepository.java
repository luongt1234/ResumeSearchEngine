package com.luontd.resumeservice.infrastructure.persistence;

import com.luontd.resumeservice.domain.entity.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface IResumeEducationJpaRepository extends JpaRepository<ResumeEducation, UUID> {

    @Modifying
    @Query("DELETE FROM ResumeEducation e WHERE e.resume.id = :resumeId")
    void deleteAllByResumeId(@Param("resumeId") UUID resumeId);
}
