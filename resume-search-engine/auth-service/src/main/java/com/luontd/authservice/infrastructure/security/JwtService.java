package com.luontd.authservice.infrastructure.security;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final Key jwtSigningKey;

    public String generateToken(UUID userId, String username, Collection<String> roles) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId.toString())
                .claim("roles", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 3600000))
                .signWith(jwtSigningKey)
                .compact();
    }
}
