package com.luontd.apigateway.config;

import com.luontd.apigateway.security.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
//import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration // Đánh dấu đây là lớp cấu hình hệ thống (Nhà máy sản xuất Bean)
public class GatewayRouteConfig {

    /**
     * Cấu hình Bản đồ định tuyến (Routing Map) tích hợp chuỗi bộ lọc Middleware tuần tự.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtFilter) {
        return builder.routes()
                // 0. [PUBLIC] Tuyến đường Health Check - KHÔNG qua JWT filter
                //    Dùng để kiểm tra resume-service qua Gateway mà không cần Bearer Token
                .route("resume-service-health", r -> r
                        .path("/api/v1/cv/health")  // Khớp chính xác endpoint health
                        .uri("http://localhost:8081")  // Forward thẳng, không qua jwtFilter
                )
                // 1. [PROTECTED] Tuyến đường xử lý CV (gửi sang resume-service)
                .route("resume-service-route", r -> r
                        .path("/api/v1/cv/**") // Điều kiện định tuyến: Khớp URI Path
                        .filters(f -> f
                                .filter(jwtFilter) // Mắt xích 1: Chạy qua Middleware xác thực JWT
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter()) // Mắt xích 2: Chặn spam API bằng Redis
//                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8081") // Đích đến: Cổng mạng vật lý của resume-service
                )
                // 2. Tuyến đường Tìm kiếm lai (gửi sang search-service)
                .route("search-service-route", r -> r
                        .path("/api/v1/search/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("http://localhost:8082") // Điều hướng sang search-service
                )
                // 3. Tuyến đường Quản lý hồ sơ ứng viên (gửi sang candidate-service)
                .route("candidate-service-route", r -> r
                        .path("/api/v1/candidates/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("http://localhost:8083") // Điều hướng sang candidate-service
                )
                .build();
    }

    /**
     * Cấu hình cấu trúc bộ giới hạn tải (Rate Limiter) sử dụng thuật toán Token Bucket của Redis.
     * - replenishRate: Số lượng token được nạp lại vào bucket trong mỗi giây (10 request/s).
     * - burstCapacity: Dung lượng tối đa của bucket, cho phép tích lũy đột biến trong 1 giây (20 request).
     */
//    @Bean
//    public RedisRateLimiter redisRateLimiter() {
//        return new RedisRateLimiter(10, 20);
//    }

    /**
     * Định danh đối tượng dùng để tính toán và chặn Rate Limit.
     * Ở đây, hệ thống dựa vào "X-User-Id" - Header nội bộ đã được JwtAuthenticationFilter tiêm (mutate) vào request.
     */
//    @Bean
//    public KeyResolver userKeyResolver() {
//        return exchange -> Mono.just(
//                Objects.requireNonNull(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
//        );
//    }
}