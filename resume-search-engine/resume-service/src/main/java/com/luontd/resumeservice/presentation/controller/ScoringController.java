package com.luontd.resumeservice.presentation.controller;

import com.luontd.resumeservice.application.dto.ScoreResultDto;
import com.luontd.resumeservice.application.interfaces.usecase.IScoreResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class ScoringController {

    private final IScoreResumeService scoreResumeService;

    /**
     * Endpoint: POST /api/v1/batches/{batchId}/score
     * Đánh giá mức độ phù hợp của toàn bộ CV trong Batch với JD.
     */
    @PostMapping("/{batchId}/score")
    public ResponseEntity<ScoreResultDto> scoreBatch(@PathVariable UUID batchId) {
        ScoreResultDto result = scoreResumeService.scoreBatch(batchId);
        return ResponseEntity.ok(result);
    }
}
