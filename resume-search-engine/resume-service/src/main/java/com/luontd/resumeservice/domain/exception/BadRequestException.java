package com.luontd.resumeservice.domain.exception;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(400, message);
    }
}
