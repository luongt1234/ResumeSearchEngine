package com.luontd.authservice.presentation.controller;

import com.luontd.authservice.application.interfaces.usecase.IAuthService;
import com.luontd.authservice.presentation.dto.ApiResponse;
import com.luontd.authservice.application.services.dto.LoginRequest; // Cần import đúng DTO
import com.luontd.authservice.application.services.dto.RegisterRequest; // Cần import đúng DTO
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final IAuthService _authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody @Valid LoginRequest request){
        try {
            var result = _authService.login(request);

            Map<String, Object> data = Map.of(
                    "token", result.getToken(),
                    "loginAt", java.time.Instant.now()
            );

            return ResponseEntity.ok(ApiResponse.success(data, "Đăng nhập thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi đăng nhập: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody @Valid RegisterRequest request){
        try {
            var result = _authService.register(request);

            Map<String, Object> data = Map.of(
                    "id", result.getId(),
                    "username", result.getUsername(),
                    "message", result.getMessage()
            );

            return ResponseEntity.ok(ApiResponse.success(data, "Đăng ký thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}