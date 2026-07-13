package com.luontd.resumeservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreBreakdownDto {
    private Double semanticScore;
    private Double skillScore;
    private Double yoeScore;
}
