package com.luontd.resumeservice.presentation.dto;

import lombok.Data;

@Data
public class ResumeBatchRequest {
    private String batchName;
    private String targetPosition;
    private Double minYoe;
    private String description;
}
