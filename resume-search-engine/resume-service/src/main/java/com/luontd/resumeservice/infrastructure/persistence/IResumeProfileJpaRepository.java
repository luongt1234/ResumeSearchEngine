package com.luontd.resumeservice.infrastructure.persistence;

import com.luontd.resumeservice.domain.entity.ResumeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IResumeProfileJpaRepository extends JpaRepository<ResumeProfile, UUID> {

    /**
     * Tìm profile theo resumeId — dùng cho upsert (xóa profile cũ trước khi ghi mới).
     * Tránh lỗi UNIQUE constraint trên column resume_id.
     */
    Optional<ResumeProfile> findByResumeId(UUID resumeId);

    List<ResumeProfile> findByResumeIdIn(List<UUID> resumeIds);
}
