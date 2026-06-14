package com.luontd.resumeservice.presentation.controller;

import com.luontd.resumeservice.application.interfaces.usecase.IUploadResumeService;
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

    private final IUploadResumeService uploadResumeService;

    /**
     * GET /api/v1/cv/health
     * Endpoint kiểm tra service đang chạy - KHÔNG qua JWT filter.
     * Dùng để test API qua Gateway nhanh chóng mà không cần token.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        try {
            Map<String, Object> info = Map.of(
                    "service", "resume-service",
                    "version", "1.0.0",
                    "status", "UP",
                    "timestamp", LocalDateTime.now().toString(),
                    "port", 8081
            );
            return ResponseEntity.ok(ApiResponse.success(info, "resume-service đang hoạt động bình thường ✅"));
        } catch (Exception e) {
            log.error("Lỗi khi gọi API health check: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/cv/upload
     * Endpoint để upload CV.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeUploadResponse>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "batchId", required = false) String batchId,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        try {
            log.info("Received request to upload resume. File: {}, batchId: {}, userId: {}", file.getOriginalFilename(), batchId, userId);

            String resumeIdStr = uploadResumeService.UploadResume(file, batchId, userId);

            Long resumeId = null;
            try {
                if (resumeIdStr != null && !resumeIdStr.isEmpty()) {
                    resumeId = Long.parseLong(resumeIdStr);
                }
            } catch (NumberFormatException e) {
                log.warn("Could not parse resume ID string to Long: {}", resumeIdStr);
            }

            ResumeUploadResponse responseData = ResumeUploadResponse.builder()
                    .resumeId(resumeId)
                    .originalFilename(file.getOriginalFilename())
                    .status("SUCCESS")
                    .message("File uploaded successfully")
                    .build();

            return ResponseEntity.ok(ApiResponse.success(responseData, "CV uploaded successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments for CV upload: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading CV: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống khi upload CV: " + e.getMessage()));
        }
    }
}
