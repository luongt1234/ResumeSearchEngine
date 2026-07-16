package com.luontd.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(org.springframework.security.config.Customizer.withDefaults()) // Tích hợp CORS vào Security Chain
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll() // Cho phép request OPTIONS (Preflight)
                .pathMatchers("/api/v1/auth/**").permitAll() // API đăng nhập/đăng ký công khai
                .pathMatchers("/api/v1/cv/health").permitAll() // Health check endpoint - không cần xác thực
                .anyExchange().permitAll()
            );
        return http.build();
    }

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Địa chỉ Frontend của bạn
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
