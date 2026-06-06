package com.luontd.resumeservice.domain.exception;

/**
 * Ném ra khi file upload không đúng định dạng cho phép (không phải PDF/DOCX).
 */
public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String message) {
        super(message);
    }
}
