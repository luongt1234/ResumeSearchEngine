package com.luontd.authservice.application.interfaces.usecase;

import com.luontd.authservice.application.services.dto.LoginRequest;
import com.luontd.authservice.application.services.dto.LoginResponse;
import com.luontd.authservice.application.services.dto.RegisterRequest;
import com.luontd.authservice.application.services.dto.RegisterResponse;

import java.util.Optional;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    RegisterResponse register(RegisterRequest request);
}
