package com.luontd.searchservice.application.interfaces;

import com.luontd.searchservice.application.dto.SkillMatchResultDto;

import java.util.List;
import java.util.Map;

public interface IWeaviateSimilarityPort {
    List<Double> getEmbedding(String text);
    Map<String, Double> getSemanticScores(List<Double> jdVector, List<String> resumeIds);
    Map<String, Double> searchCandidates(List<Double> queryVector, int limit);
    Map<String, SkillMatchResultDto> getBestSkillMatches(List<Double> batchSkillVector, List<String> resumeIds);
}
