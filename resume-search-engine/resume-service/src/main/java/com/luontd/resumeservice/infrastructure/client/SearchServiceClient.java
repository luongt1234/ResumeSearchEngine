package com.luontd.resumeservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "search-service", path = "/api/internal/search")
public interface SearchServiceClient {

    @PostMapping("/embedding")
    List<Double> getEmbedding(@RequestBody String text);

    @PostMapping("/semantic-score")
    Map<String, Double> getSemanticScores(@RequestBody SemanticScoreRequest req);

    @PostMapping("/skill-match")
    Map<String, SkillMatchResult> getSkillMatches(@RequestBody SemanticScoreRequest req);

    record SemanticScoreRequest(List<Double> vector, List<String> resumeIds) {}
    record SkillMatchResult(String matchedSkillName, Double certainty) {}
}
