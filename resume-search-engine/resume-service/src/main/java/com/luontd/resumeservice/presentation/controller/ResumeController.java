package com.luontd.resumeservice.presentation.controller;

import com.luontd.resumeservice.presentation.dto.ApiResponse;
import com.luontd.resumeservice.presentation.dto.ResumeDetailResponse;
import com.luontd.resumeservice.presentation.dto.ResumeUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Presentation Layer: REST Controller cho CV (Resume) APIs.
 * Base path: /api/v1/cv  → được API Gateway route tới service này.
 */
@RestController
@RequestMapping("/api/v1/cv")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {
    /**
     * GET /api/v1/cv/health
     * Endpoint kiểm tra service đang chạy - KHÔNG qua JWT filter.
     * Dùng để test API qua Gateway nhanh chóng mà không cần token.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> info = Map.of(
                "service", "resume-service",
                "version", "1.0.0",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "port", 8081
        );
        return ResponseEntity.ok(ApiResponse.success(info, "resume-service đang hoạt động bình thường ✅"));
    }
}
