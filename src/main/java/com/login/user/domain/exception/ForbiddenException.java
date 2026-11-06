package com.login.user.domain.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("You do not have permission to access this resource");
    }
}
