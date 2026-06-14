package com.luontd.resumeservice.presentation.dto;

import lombok.Data;

@Data
public class BatchSkillRequest {
    private String skillName;
    private Integer weight;
    private Boolean isMandatory;
}
