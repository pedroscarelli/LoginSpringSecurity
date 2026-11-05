package com.login.user.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.login.user.domain.dto.request.LoginRequestDTO;
import com.login.user.domain.exception.IncorrectCredentialsException;
import com.login.user.domain.exception.UnauthorizedException;
import com.login.user.domain.exception.UserNotActivatedException;
import com.login.user.domain.exception.UserNotFoundException;
import com.login.user.domain.model.User;
import com.login.user.util.SecureTokens;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AuthenticationService {

    static final Duration OTP_VALIDITY = Duration.ofMinutes(5);
    static final int MAX_OTP_ATTEMPTS = 5;

    private AuthenticationManager authenticationManager;

    private UserService userService;

    private EmailService emailService;

    public User authenticateLogin(LoginRequestDTO loginRequestDto) {
        User user;
        try {
            user = userService.getUserByEmail(loginRequestDto.email());
            var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password());
            authenticationManager.authenticate(authenticationToken);
        } catch (UserNotFoundException | AuthenticationException exception) {
            throw new IncorrectCredentialsException("Incorrect login or password");
        }

        // Only reveal that the account is not activated after the password was checked.
        if (!user.isEnabled()) {
            throw new UserNotActivatedException();
        }

        return user;
    }

    /**
     * Sends a reset code when the e-mail exists. It answers the same way when it doesn't,
     * so the endpoint can't be used to find out which e-mails are registered.
     */
    public void activateRedefinePassword(String email) {
        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (UserNotFoundException exception) {
            return;
        }

        var otpCode = SecureTokens.otpCode();
        user.setOtpCode(otpCode);
        user.setOtpTimestamp(Instant.now());
        user.setOtpAttempts(0);
        userService.save(user);

        emailService.sendRedefinePasswordEmail(user.getEmail(), otpCode);
    }

    public void redefinePassword(String otpCode, String newPassword, String email) {
        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (UserNotFoundException exception) {
            throw passwordResetNotAuthorized();
        }

        verifyOtpCode(user, otpCode);

        user.setPassword(userService.encodePassword(newPassword));
        clearOtp(user);
        userService.save(user);
    }

    void verifyOtpCode(User user, String incomingOtpCode) {
        if (user.getOtpCode() == null || user.getOtpTimestamp() == null) {
            throw passwordResetNotAuthorized();
        }

        var expired = Instant.now().isAfter(user.getOtpTimestamp().plus(OTP_VALIDITY));
        if (expired || user.getOtpAttempts() >= MAX_OTP_ATTEMPTS) {
            clearOtp(user);
            userService.save(user);
            throw passwordResetNotAuthorized();
        }

        if (!SecureTokens.constantTimeEquals(user.getOtpCode(), incomingOtpCode)) {
            user.setOtpAttempts(user.getOtpAttempts() + 1);
            userService.save(user);
            throw passwordResetNotAuthorized();
        }
    }

    private void clearOtp(User user) {
        user.setOtpCode(null);
        user.setOtpTimestamp(null);
        user.setOtpAttempts(0);
    }

    private UnauthorizedException passwordResetNotAuthorized() {
        return new UnauthorizedException("Password reset not authorized");
    }
}
