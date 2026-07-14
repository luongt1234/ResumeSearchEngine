package com.luontd.authservice.domain.exception;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(400, message);
    }
}
