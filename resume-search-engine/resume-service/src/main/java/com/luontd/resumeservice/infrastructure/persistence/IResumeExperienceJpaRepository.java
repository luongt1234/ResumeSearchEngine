package com.luontd.resumeservice.infrastructure.persistence;

import com.luontd.resumeservice.domain.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface IResumeExperienceJpaRepository extends JpaRepository<ResumeExperience, UUID> {

    @Modifying
    @Query("DELETE FROM ResumeExperience e WHERE e.resume.id = :resumeId")
    void deleteAllByResumeId(@Param("resumeId") UUID resumeId);
}
