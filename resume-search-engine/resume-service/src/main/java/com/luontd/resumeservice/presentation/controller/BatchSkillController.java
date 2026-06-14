package com.luontd.resumeservice.presentation.controller;

import com.luontd.resumeservice.application.interfaces.usecase.IBatchSkillService;
import com.luontd.resumeservice.presentation.dto.ApiResponse;
import com.luontd.resumeservice.presentation.dto.BatchSkillRequest;
import com.luontd.resumeservice.presentation.dto.BatchSkillResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batches/{batchId}/skills")
@RequiredArgsConstructor
@Slf4j
public class BatchSkillController {

    private final IBatchSkillService batchSkillService;

    @PostMapping
    public ResponseEntity<ApiResponse<BatchSkillResponse>> addSkill(
            @PathVariable UUID batchId,
            @RequestBody BatchSkillRequest request) {
        log.info("REST request to add skill to batch: {}", batchId);
        try {
            BatchSkillResponse response = batchSkillService.addSkillToBatch(batchId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Skill added to batch successfully"));
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
    public ResponseEntity<ApiResponse<List<BatchSkillResponse>>> getSkillsByBatchId(@PathVariable UUID batchId) {
        log.info("REST request to get skills for batch: {}", batchId);
        try {
            List<BatchSkillResponse> responses = batchSkillService.getSkillsByBatchId(batchId);
            return ResponseEntity.ok(ApiResponse.success(responses, "Skills retrieved successfully"));
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

    @PutMapping("/{skillId}")
    public ResponseEntity<ApiResponse<BatchSkillResponse>> updateSkill(
            @PathVariable UUID batchId,
            @PathVariable UUID skillId,
            @RequestBody BatchSkillRequest request) {
        log.info("REST request to update skill {} in batch {}", skillId, batchId);
        try {
            BatchSkillResponse response = batchSkillService.updateBatchSkill(batchId, skillId, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Skill updated successfully"));
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

    @DeleteMapping("/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeSkill(
            @PathVariable UUID batchId,
            @PathVariable UUID skillId) {
        log.info("REST request to delete skill {} from batch {}", skillId, batchId);
        try {
            batchSkillService.removeSkillFromBatch(batchId, skillId);
            return ResponseEntity.ok(ApiResponse.success(null, "Skill removed from batch successfully"));
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
