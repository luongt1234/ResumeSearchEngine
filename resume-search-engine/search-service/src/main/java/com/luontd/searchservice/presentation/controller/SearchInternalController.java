package com.luontd.searchservice.presentation.controller;

import com.luontd.searchservice.infrastructure.weaviate.WeaviateSimilarityClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/search")
@RequiredArgsConstructor
public class SearchInternalController {

    private final WeaviateSimilarityClient weaviateClient;

    @PostMapping("/embedding")
    public ResponseEntity<List<Double>> getEmbedding(@RequestBody String text) {
        return ResponseEntity.ok(weaviateClient.getEmbedding(text));
    }

    @PostMapping("/semantic-score")
    public ResponseEntity<Map<String, Double>> getSemanticScores(@RequestBody SemanticScoreRequest req) {
        return ResponseEntity.ok(weaviateClient.getSemanticScores(req.getVector(), req.getResumeIds()));
    }

    @PostMapping("/skill-match")
    public ResponseEntity<Map<String, WeaviateSimilarityClient.SkillMatchResult>> getSkillMatches(@RequestBody SemanticScoreRequest req) {
        return ResponseEntity.ok(weaviateClient.getBestSkillMatches(req.getVector(), req.getResumeIds()));
    }

    @Data
    public static class SemanticScoreRequest {
        private List<Double> vector;
        private List<String> resumeIds;
    }
}
