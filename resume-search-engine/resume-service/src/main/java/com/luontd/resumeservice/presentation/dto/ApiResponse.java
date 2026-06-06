package com.luontd.resumeservice.presentation.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO wrapper chuẩn cho mọi response của API.
 * Đảm bảo tính nhất quán của API Response Contract.
 */
@Data
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}
