package com.login.user.domain.exception;

public class DuplicateCredentialsException extends RuntimeException {

    public DuplicateCredentialsException() {
        super("Duplicate e-mail or login.");
    }
}
