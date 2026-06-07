package com.luontd.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GatewayFilter {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Kiểm tra sự tồn tại của Header Authorization
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.error("[Auth Middleware] Từ chối truy cập: Thiếu Header Authorization");
            return this.onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 2. Kiểm tra định dạng chuẩn "Bearer <Token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("[Auth Middleware] Từ chối truy cập: Sai định dạng Bearer Token");
            return this.onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            // 3. Giải mã Token bằng thư viện JJWT
            SecretKey key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Subject = username (theo generate: .setSubject(username))
            String username = claims.getSubject();

            // Claim "id" = UUID của user (theo generate: .claim("id", userId.toString()))
            String userId = claims.get("id", String.class);

            // Claim "roles" = List<String> (theo generate: .claim("roles", roles))
            @SuppressWarnings("unchecked")
            java.util.List<String> userRoles = claims.get("roles", java.util.List.class);
            String rolesJoined = (userRoles != null) ? String.join(",", userRoles) : "";

            log.info("[Auth Middleware] Token hợp lệ! UserId: {}, Username: {}, Roles: {}", userId, username, rolesJoined);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)          // UUID thực sự của user
                    .header("X-Username", username)        // Username
                    .header("X-User-Roles", rolesJoined)  // Danh sách role, phân tách bằng dấu phẩy
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("[Auth Middleware] Từ chối truy cập: Token không hợp lệ hoặc hết hạn. Lỗi: {}", e.getMessage());
            return this.onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete(); // Ngắt kết nối bất đồng bộ lập tức
    }
}