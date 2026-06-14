package com.luontd.resumeservice.presentation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResumeBatchResponse {
    private UUID id;
    private String batchName;
    private String targetPosition;
    private Double minYoe;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
