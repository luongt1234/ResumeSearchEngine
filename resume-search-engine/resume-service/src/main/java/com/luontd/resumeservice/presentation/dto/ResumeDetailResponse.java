package com.luontd.resumeservice.presentation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO trả về chi tiết thông tin một CV.
 */
@Data
@Builder
public class ResumeDetailResponse {
    private Long id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String storagePath;
    private String uploadedBy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
