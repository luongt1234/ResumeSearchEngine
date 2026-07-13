package com.luontd.resumeservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResultDto {
    private UUID batchId;
    private String batchName;
    private int totalCandidates;
    private List<CandidateScoreDto> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateScoreDto {
        private UUID resumeId;
        private String candidateName;
        private Double finalScore;
        private String label; // Excellent, Good, Fair, Poor
        private boolean hasMissingMandatory;

        private ScoreBreakdownDto breakdown;

        private List<SkillMatchDto> skillMatches;
        private List<String> missingSkills;
        private List<String> missingMandatorySkills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillMatchDto {
        private String requiredSkill;
        private String matchedWith;
        private Double certainty;
        private boolean isMandatory;
    }
}
