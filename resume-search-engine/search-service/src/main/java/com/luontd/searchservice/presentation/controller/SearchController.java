package com.luontd.searchservice.presentation.controller;

import com.luontd.searchservice.application.dto.CandidateResponse;
import com.luontd.searchservice.application.services.HybridSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final HybridSearchService hybridSearchService;

    @GetMapping("/candidates")
    public ResponseEntity<List<CandidateResponse>> searchCandidates(
            @RequestParam(name = "q", defaultValue = "") String query) {
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<CandidateResponse> results = hybridSearchService.search(query);
        return ResponseEntity.ok(results);
    }
}
