package com.luontd.resumeservice.presentation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BatchSkillResponse {
    private UUID id;
    private UUID batchId;
    private String skillName;
    private Integer weight;
    private Boolean isMandatory;
}
