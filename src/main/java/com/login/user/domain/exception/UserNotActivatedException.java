package com.login.user.domain.exception;

public class UserNotActivatedException extends RuntimeException {

    public UserNotActivatedException() {
        super("User not activated");
    }
}
