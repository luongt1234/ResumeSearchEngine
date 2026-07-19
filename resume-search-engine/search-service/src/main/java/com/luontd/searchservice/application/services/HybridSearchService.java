package com.luontd.searchservice.application.services;

import com.luontd.searchservice.application.dto.CandidateResponse;
import com.luontd.searchservice.infrastructure.elasticsearch.CandidateDocument;
import com.luontd.searchservice.infrastructure.elasticsearch.CandidateSearchRepository;
import com.luontd.searchservice.application.interfaces.IWeaviateSimilarityPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HybridSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final CandidateSearchRepository candidateSearchRepository;
    private final IWeaviateSimilarityPort weaviateClient;

    private static final int RRF_K = 60;
    private static final int FETCH_LIMIT = 50;

    public List<CandidateResponse> search(String query) {
        log.info("Starting Hybrid Search for query: {}", query);

        // 1. Keyword Search (Elasticsearch)
        CompletableFuture<List<SearchHit<CandidateDocument>>> esFuture = CompletableFuture.supplyAsync(() -> {
            Criteria criteria = new Criteria("summary").matches(query)
                    .or(new Criteria("skills").matches(query))
                    .or(new Criteria("experienceText").matches(query));
            Query esQuery = new CriteriaQuery(criteria);
            // Sử dụng setPageable thay vì setMaxResults trong Spring Data Elasticsearch
            esQuery.setPageable(org.springframework.data.domain.PageRequest.of(0, FETCH_LIMIT));
            
            SearchHits<CandidateDocument> searchHits = elasticsearchOperations.search(esQuery, CandidateDocument.class);
            return searchHits.getSearchHits();
        });

        // 2. Semantic Search (Weaviate)
        CompletableFuture<Map<String, Double>> weaviateFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<Double> queryVector = weaviateClient.getEmbedding(query);
                return weaviateClient.searchCandidates(queryVector, FETCH_LIMIT);
            } catch (Exception e) {
                log.error("Weaviate search failed", e);
                return Collections.emptyMap();
            }
        });

        // Wait for both
        CompletableFuture.allOf(esFuture, weaviateFuture).join();

        List<SearchHit<CandidateDocument>> esResults = esFuture.join();
        Map<String, Double> weaviateResults = weaviateFuture.join();

        // 3. Tính toán RRF
        Map<String, RrfScore> rrfScores = new HashMap<>();

        // Gán rank cho kết quả ES
        for (int i = 0; i < esResults.size(); i++) {
            SearchHit<CandidateDocument> hit = esResults.get(i);
            String resumeId = hit.getContent().getResumeId();
            rrfScores.putIfAbsent(resumeId, new RrfScore(hit.getContent()));
            RrfScore rrf = rrfScores.get(resumeId);
            rrf.esRank = i + 1;
            rrf.keywordScore = hit.getScore();
        }

        // Gán rank cho kết quả Weaviate
        // Sort Weaviate results by certainty descending
        List<Map.Entry<String, Double>> sortedWeaviate = new ArrayList<>(weaviateResults.entrySet());
        sortedWeaviate.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        for (int i = 0; i < sortedWeaviate.size(); i++) {
            Map.Entry<String, Double> entry = sortedWeaviate.get(i);
            String resumeId = entry.getKey();
            if (!rrfScores.containsKey(resumeId)) {
                // Fetch document from ES if it was only found in Weaviate
                candidateSearchRepository.findById(resumeId).ifPresent(doc -> {
                    rrfScores.put(resumeId, new RrfScore(doc));
                });
            }
            if (rrfScores.containsKey(resumeId)) {
                RrfScore rrf = rrfScores.get(resumeId);
                rrf.weaviateRank = i + 1;
                rrf.semanticScore = entry.getValue();
            }
        }

        // Calculate final RRF
        List<RrfScore> finalScores = new ArrayList<>(rrfScores.values());
        for (RrfScore score : finalScores) {
            double rrf = 0.0;
            if (score.esRank > 0) {
                rrf += 1.0 / (RRF_K + score.esRank);
            }
            if (score.weaviateRank > 0) {
                rrf += 1.0 / (RRF_K + score.weaviateRank);
            }
            score.finalScore = rrf;
        }

        // Sort by RRF descending
        finalScores.sort((a, b) -> Double.compare(b.finalScore, a.finalScore));

        // Map to Response
        return finalScores.stream().map(score -> {
            CandidateDocument doc = score.doc;
            return CandidateResponse.builder()
                    .resumeId(doc.getResumeId())
                    .fullName(doc.getFullName())
                    .email(doc.getEmail())
                    .phone(doc.getPhone())
                    .skills(doc.getSkills())
                    .summary(doc.getSummary())
                    .hybridScore(score.finalScore)
                    .keywordScore(score.keywordScore)
                    .semanticScore(score.semanticScore)
                    .build();
        }).collect(Collectors.toList());
    }

    private static class RrfScore {
        CandidateDocument doc;
        int esRank = 0;
        int weaviateRank = 0;
        double keywordScore = 0.0;
        double semanticScore = 0.0;
        double finalScore = 0.0;

        public RrfScore(CandidateDocument doc) {
            this.doc = doc;
        }
    }
}
