package com.luontd.resumeservice.presentation.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO trả về sau khi upload CV thành công.
 */
@Data
@Builder
public class ResumeUploadResponse {
    private Long resumeId;
    private String originalFilename;
    private String storagePath;
    private String status;
    private String message;
}
