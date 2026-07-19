package com.luontd.authservice.application.services.auth;

import com.luontd.authservice.application.interfaces.usecase.IAuthService;
import com.luontd.authservice.application.interfaces.repository.IUserRepository;
import com.luontd.authservice.application.interfaces.repository.IRoleRepository;
import com.luontd.authservice.application.mapper.IUserMapper;
import com.luontd.authservice.application.services.dto.LoginRequest;
import com.luontd.authservice.application.services.dto.LoginResponse;
import com.luontd.authservice.application.services.dto.RegisterRequest;
import com.luontd.authservice.application.services.dto.RegisterResponse;
import com.luontd.authservice.domain.entity.Role;
import com.luontd.authservice.application.interfaces.IJwtPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final IUserRepository _userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IJwtPort _jwtService;
    private final IRoleRepository _roleRepository;
    private final IUserMapper _userMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        var user = _userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Tài khoản hoặc mật khẩu không chính xác"));

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác");
        }

        var rolesByUser = _roleRepository.findByUserId(user.getId())
                .orElse(Collections.emptyList());

        var rolesname = rolesByUser
                .stream()
                .map(Role::getName)
                .toList();

        // generate token
        String token = _jwtService.generateToken(user.getId(),user.getUsername(), rolesname);

        return new LoginResponse(token);
    }

    @Override
    public RegisterResponse register(RegisterRequest request){
        var existsUser = _userRepository.existsByUsername(request.getUsername());
        var existsEmail = _userRepository.existsByEmail(request.getEmail());
        if (existsUser){
            throw new RuntimeException("Username đã tồn tại");
        }

        if (existsEmail){
            throw new RuntimeException("Email đã tồn tại");
        }

        // map request -> entity
        var userEntity = _userMapper.toEntity(request);

        // hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        userEntity.setPassword(hashedPassword);

        // save user
        _userRepository.save(userEntity);

        // map lại entity -> response
        return _userMapper.toResponse(userEntity);
    }
}
