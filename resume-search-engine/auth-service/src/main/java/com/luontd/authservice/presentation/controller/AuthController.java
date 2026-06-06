package com.luontd.authservice.presentation.controller;

import com.luontd.authservice.presentation.dto.ApiResponse;
import com.luontd.authservice.presentation.dto.request.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> Login(@RequestBody @Valid LoginRequest request){
        
    }
}
