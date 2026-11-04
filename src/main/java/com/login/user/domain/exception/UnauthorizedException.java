package com.login.user.domain.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("User not authenticated");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
