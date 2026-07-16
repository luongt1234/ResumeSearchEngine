package com.luontd.searchservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private String resumeId;
    private String fullName;
    private String email;
    private String phone;
    private List<String> skills;
    private String summary;
    
    // Scores
    private double hybridScore;
    private double semanticScore;
    private double keywordScore;
}
