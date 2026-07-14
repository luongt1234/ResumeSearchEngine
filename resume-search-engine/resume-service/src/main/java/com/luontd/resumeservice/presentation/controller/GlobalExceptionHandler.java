package com.luontd.resumeservice.presentation.controller;

import com.luontd.resumeservice.domain.exception.InvalidFileTypeException;
import com.luontd.resumeservice.domain.exception.ResumeNotFoundException;
import com.luontd.resumeservice.presentation.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Global Exception Handler: Bắt và xử lý tập trung mọi exception trong service.
 * Đảm bảo API luôn trả về JSON theo chuẩn ApiResponse.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResumeNotFoundException ex) {
        log.warn("[ExceptionHandler] 404 Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFile(InvalidFileTypeException ex) {
        log.warn("[ExceptionHandler] 400 Bad Request - Invalid file type: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileTooLarge(MaxUploadSizeExceededException ex) {
        log.warn("[ExceptionHandler] 400 - File too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "File quá lớn! Kích thước tối đa cho phép là 10MB."));
    }

    @ExceptionHandler(com.luontd.resumeservice.domain.exception.BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(com.luontd.resumeservice.domain.exception.BusinessException ex) {
        log.warn("[ExceptionHandler] BusinessException {}: {}", ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getStatus(), ex.getMessage()));
    }

    @ExceptionHandler(com.luontd.resumeservice.domain.exception.BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(com.luontd.resumeservice.domain.exception.BadRequestException ex) {
        log.warn("[ExceptionHandler] 400 Bad Request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(com.luontd.resumeservice.domain.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(com.luontd.resumeservice.domain.exception.ResourceNotFoundException ex) {
        log.warn("[ExceptionHandler] 404 Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("[ExceptionHandler] 500 Internal Error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Lỗi hệ thống nội bộ. Vui lòng thử lại sau."));
    }
}
