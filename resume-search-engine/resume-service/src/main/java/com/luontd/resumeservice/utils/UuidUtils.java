package com.luontd.resumeservice.utils;

import java.util.Optional;
import java.util.UUID;

public class UuidUtils {
    public static Optional<UUID> tryParse(String uuidString) {
        // Kiểm tra null hoặc rỗng trước để đỡ phải vào try-catch
        if (uuidString == null || uuidString.isBlank()) {
            return Optional.empty();
        }
        try {
            // Cố gắng parse
            return Optional.of(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            // Parse thất bại (chuỗi không đúng định dạng UUID)
            return Optional.empty();
        }
    }
}
