package com.luontd.resumeservice.domain.exception;

/**
 * Ném ra khi không tìm thấy Resume theo ID trong database.
 */
public class ResumeNotFoundException extends RuntimeException {
    public ResumeNotFoundException(Long id) {
        super("Không tìm thấy CV với ID: " + id);
    }
}
