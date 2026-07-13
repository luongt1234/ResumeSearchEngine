package com.luontd.resumeservice.application.services;

import com.luontd.resumeservice.application.dto.ScoreBreakdownDto;
import com.luontd.resumeservice.application.dto.ScoreResultDto;
import com.luontd.resumeservice.application.interfaces.usecase.IScoreResumeService;
import com.luontd.resumeservice.domain.entity.*;
import com.luontd.resumeservice.domain.enums.EtlStatus;
import com.luontd.resumeservice.infrastructure.persistence.IResumeBatchJpaRepository;
import com.luontd.resumeservice.infrastructure.persistence.IResumeJpaRepository;
import com.luontd.resumeservice.infrastructure.persistence.IResumeProfileJpaRepository;
import com.luontd.resumeservice.infrastructure.persistence.IResumeSkillJpaRepository;
import com.luontd.resumeservice.infrastructure.client.SearchServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreResumeService implements IScoreResumeService {

    private final IResumeBatchJpaRepository batchRepository;
    private final IResumeJpaRepository resumeRepository;
    private final IResumeProfileJpaRepository profileRepository;
    private final IResumeSkillJpaRepository skillRepository;
    private final SearchServiceClient searchServiceClient;

    @Value("${scoring.weights.semantic:0.50}")
    private Double weightSemantic;

    @Value("${scoring.weights.skill:0.35}")
    private Double weightSkill;

    @Value("${scoring.weights.yoe:0.15}")
    private Double weightYoe;

    @Value("${scoring.thresholds.skill-similarity:0.85}")
    private Double skillSimilarityThreshold;

    @Override
    public ScoreResultDto scoreBatch(UUID batchId) {
        log.info("Bắt đầu chấm điểm cho Batch ID: {}", batchId);

        // 1. Lấy thông tin Batch & Kỹ năng yêu cầu
        ResumeBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Batch với ID: " + batchId));

        List<BatchSkill> requiredSkills = batch.getRequiredSkills();

        // 2. Lấy danh sách Resume đã xử lý xong (COMPLETED) trong batch
        List<Resume> resumes = resumeRepository.findByBatchIdAndEtlStatus(batchId, EtlStatus.COMPLETED);
        if (resumes.isEmpty()) {
            return ScoreResultDto.builder()
                    .batchId(batchId)
                    .batchName(batch.getBatchName())
                    .totalCandidates(0)
                    .results(Collections.emptyList())
                    .build();
        }

        List<UUID> resumeIds = resumes.stream().map(Resume::getId).collect(Collectors.toList());
        List<String> resumeIdStrings = resumeIds.stream().map(UUID::toString).collect(Collectors.toList());

        // 3. Lấy thông tin Profile (YoE) và Skills từ MySQL
        List<ResumeProfile> profiles = profileRepository.findByResumeIdIn(resumeIds);
        Map<UUID, Double> resumeYoeMap = profiles.stream()
                .filter(p -> p.getYoe() != null)
                .collect(Collectors.toMap(p -> p.getResume().getId(), ResumeProfile::getYoe, (y1, y2) -> y1));

        // 4. Semantic Score (Layer 1) - Tạo JD Context và so sánh Weaviate qua search-service
        String jdText = String.format("%s: %s",
                batch.getTargetPosition() != null ? batch.getTargetPosition() : "",
                batch.getDescription() != null ? batch.getDescription() : "");
        
        List<Double> jdVector = searchServiceClient.getEmbedding(jdText);
        Map<String, Double> semanticScoresMap = searchServiceClient.getSemanticScores(
                new SearchServiceClient.SemanticScoreRequest(jdVector, resumeIdStrings));

        // 5. Semantic Skill Score (Layer 2) - Lấy vector cho từng required skill và query qua search-service
        Map<String, Map<String, SearchServiceClient.SkillMatchResult>> batchSkillMatchesByResumeId = new HashMap<>();
        // Khởi tạo map cho mỗi resumeId
        for (String id : resumeIdStrings) {
            batchSkillMatchesByResumeId.put(id, new HashMap<>());
        }

        for (BatchSkill reqSkill : requiredSkills) {
            List<Double> reqSkillVector = searchServiceClient.getEmbedding(reqSkill.getSkillName());
            Map<String, SearchServiceClient.SkillMatchResult> bestMatchesForThisSkill = 
                    searchServiceClient.getSkillMatches(new SearchServiceClient.SemanticScoreRequest(reqSkillVector, resumeIdStrings));
            
            for (Map.Entry<String, SearchServiceClient.SkillMatchResult> entry : bestMatchesForThisSkill.entrySet()) {
                String resumeIdStr = entry.getKey();
                SearchServiceClient.SkillMatchResult match = entry.getValue();
                
                batchSkillMatchesByResumeId.get(resumeIdStr).put(reqSkill.getSkillName(), match);
            }
        }

        // 6. Tính toán điểm cho từng CV
        List<ScoreResultDto.CandidateScoreDto> candidateScores = new ArrayList<>();
        double totalSkillWeight = requiredSkills.stream().mapToDouble(BatchSkill::getWeight).sum();

        for (Resume resume : resumes) {
            UUID rId = resume.getId();
            String rIdStr = rId.toString();

            // Tính Semantic Score
            Double certainty = semanticScoresMap.getOrDefault(rIdStr, 0.0);
            Double semanticScore = certainty * 100.0;

            // Tính Skill Score
            double matchedWeight = 0.0;
            List<ScoreResultDto.SkillMatchDto> skillMatchDtos = new ArrayList<>();
            List<String> missingSkills = new ArrayList<>();
            List<String> missingMandatorySkills = new ArrayList<>();
            boolean hasMissingMandatory = false;

            Map<String, WeaviateSimilarityClient.SkillMatchResult> resumeSkillMatches = batchSkillMatchesByResumeId.get(rIdStr);

            for (BatchSkill reqSkill : requiredSkills) {
                WeaviateSimilarityClient.SkillMatchResult match = resumeSkillMatches.get(reqSkill.getSkillName());
                
                if (match != null && match.certainty() >= skillSimilarityThreshold) {
                    matchedWeight += reqSkill.getWeight();
                    skillMatchDtos.add(ScoreResultDto.SkillMatchDto.builder()
                            .requiredSkill(reqSkill.getSkillName())
                            .matchedWith(match.matchedSkillName())
                            .certainty(match.certainty())
                            .isMandatory(reqSkill.getIsMandatory())
                            .build());
                } else {
                    missingSkills.add(reqSkill.getSkillName());
                    if (Boolean.TRUE.equals(reqSkill.getIsMandatory())) {
                        missingMandatorySkills.add(reqSkill.getSkillName());
                        hasMissingMandatory = true;
                    }
                }
            }

            Double skillScore = totalSkillWeight > 0 ? (matchedWeight / totalSkillWeight) * 100.0 : 100.0;

            // Tính YoE Score
            Double yoeScore = 100.0;
            Double minYoe = batch.getMinYoe();
            if (minYoe != null && minYoe > 0) {
                Double candidateYoe = resumeYoeMap.get(rId);
                if (candidateYoe == null) {
                    yoeScore = 50.0; // Neutral if unknown
                } else if (candidateYoe >= minYoe) {
                    yoeScore = 100.0;
                } else {
                    yoeScore = Math.min((candidateYoe / minYoe) * 100.0, 99.0);
                }
            }

            // Tính Final Score
            Double finalScore = (semanticScore * weightSemantic) + (skillScore * weightSkill) + (yoeScore * weightYoe);

            if (hasMissingMandatory) {
                finalScore *= 0.6; // Áp dụng penalty 40%
            }

            String label = getLabel(finalScore);

            candidateScores.add(ScoreResultDto.CandidateScoreDto.builder()
                    .resumeId(rId)
                    .candidateName(getProfileName(profiles, rId))
                    .finalScore(finalScore)
                    .label(label)
                    .hasMissingMandatory(hasMissingMandatory)
                    .breakdown(ScoreBreakdownDto.builder()
                            .semanticScore(semanticScore)
                            .skillScore(skillScore)
                            .yoeScore(yoeScore)
                            .build())
                    .skillMatches(skillMatchDtos)
                    .missingSkills(missingSkills)
                    .missingMandatorySkills(missingMandatorySkills)
                    .build());
        }

        // Sort by final score DESC
        candidateScores.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));

        return ScoreResultDto.builder()
                .batchId(batchId)
                .batchName(batch.getBatchName())
                .totalCandidates(resumes.size())
                .results(candidateScores)
                .build();
    }

    private String getLabel(Double score) {
        if (score >= 85) return "Excellent";
        if (score >= 70) return "Good";
        if (score >= 50) return "Fair";
        return "Poor";
    }

    private String getProfileName(List<ResumeProfile> profiles, UUID resumeId) {
        return profiles.stream()
                .filter(p -> p.getResume().getId().equals(resumeId))
                .findFirst()
                .map(ResumeProfile::getFullName)
                .orElse("Unknown Candidate");
    }
}
