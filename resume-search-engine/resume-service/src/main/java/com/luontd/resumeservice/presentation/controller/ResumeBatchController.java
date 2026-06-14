package com.luontd.resumeservice.presentation.controller;

import com.luontd.resumeservice.application.interfaces.usecase.IResumeBatchService;
import com.luontd.resumeservice.presentation.dto.ApiResponse;
import com.luontd.resumeservice.presentation.dto.ResumeBatchRequest;
import com.luontd.resumeservice.presentation.dto.ResumeBatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@Slf4j
public class ResumeBatchController {

    private final IResumeBatchService resumeBatchService;

    @PostMapping
    public ResponseEntity<ApiResponse<ResumeBatchResponse>> createBatch(@RequestBody ResumeBatchRequest request) {
        log.info("REST request to save ResumeBatch");
        try {
            ResumeBatchResponse response = resumeBatchService.createBatch(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Batch created successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("System error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "System error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeBatchResponse>>> getAllBatches() {
        log.info("REST request to get all ResumeBatches");
        try {
            List<ResumeBatchResponse> responses = resumeBatchService.getAllBatches();
            return ResponseEntity.ok(ApiResponse.success(responses, "Batches retrieved successfully"));
        } catch (Exception e) {
            log.error("System error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "System error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeBatchResponse>> getBatchById(@PathVariable UUID id) {
        log.info("REST request to get ResumeBatch : {}", id);
        try {
            ResumeBatchResponse response = resumeBatchService.getBatchById(id);
            return ResponseEntity.ok(ApiResponse.success(response, "Batch retrieved successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("System error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "System error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeBatchResponse>> updateBatch(
            @PathVariable UUID id,
            @RequestBody ResumeBatchRequest request) {
        log.info("REST request to update ResumeBatch : {}", id);
        try {
            ResumeBatchResponse response = resumeBatchService.updateBatch(id, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Batch updated successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments or not found: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("System error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "System error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable UUID id) {
        log.info("REST request to delete ResumeBatch : {}", id);
        try {
            resumeBatchService.deleteBatch(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Batch deleted successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("System error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "System error: " + e.getMessage()));
        }
    }
}
