package com.login.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import com.login.user.domain.dto.request.LoginRequestDTO;
import com.login.user.domain.exception.IncorrectCredentialsException;
import com.login.user.domain.exception.UnauthorizedException;
import com.login.user.domain.exception.UserNotActivatedException;
import com.login.user.domain.exception.UserNotFoundException;
import com.login.user.domain.model.User;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String EMAIL = "john@example.com";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(EMAIL);
        user.setEnabled(true);
    }

    @Test
    void loginWithUnknownEmailIsIncorrectCredentials() {
        when(userService.getUserByEmail(EMAIL)).thenThrow(new UserNotFoundException());

        assertThrows(IncorrectCredentialsException.class,
            () -> authenticationService.authenticateLogin(new LoginRequestDTO(EMAIL, "pw")));
    }

    @Test
    void loginWithWrongPasswordIsIncorrectCredentials() {
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(IncorrectCredentialsException.class,
            () -> authenticationService.authenticateLogin(new LoginRequestDTO(EMAIL, "wrong")));
    }

    @Test
    void inactiveAccountIsOnlyRevealedAfterThePasswordIsChecked() {
        user.setEnabled(false);
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(IncorrectCredentialsException.class,
            () -> authenticationService.authenticateLogin(new LoginRequestDTO(EMAIL, "wrong")));
    }

    @Test
    void inactiveAccountWithRightPasswordIsNotActivated() {
        user.setEnabled(false);
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);

        assertThrows(UserNotActivatedException.class,
            () -> authenticationService.authenticateLogin(new LoginRequestDTO(EMAIL, "right")));
    }

    @Test
    void loginSuccess() {
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);

        assertThat(authenticationService.authenticateLogin(new LoginRequestDTO(EMAIL, "right"))).isSameAs(user);
    }

    @Test
    void resetRequestForUnknownEmailDoesNotRevealIt() {
        when(userService.getUserByEmail("nobody@example.com")).thenThrow(new UserNotFoundException());

        authenticationService.activateRedefinePassword("nobody@example.com");

        verify(emailService, never()).sendRedefinePasswordEmail(anyString(), anyString());
    }

    @Test
    void resetRequestGeneratesSixDigitCode() {
        user.setOtpAttempts(3);
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);

        authenticationService.activateRedefinePassword(EMAIL);

        assertThat(user.getOtpCode()).matches("\\d{6}");
        assertThat(user.getOtpAttempts()).isZero();
        verify(emailService).sendRedefinePasswordEmail(EMAIL, user.getOtpCode());
    }

    @Test
    void resetWithoutRequestingACodeIsRejectedInsteadOfCrashing() {
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);

        assertThrows(UnauthorizedException.class,
            () -> authenticationService.redefinePassword("123456", "new-password", EMAIL));
    }

    @Test
    void wrongCodeCountsAnAttempt() {
        withCode("123456", Instant.now());
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);

        assertThrows(UnauthorizedException.class,
            () -> authenticationService.redefinePassword("000000", "new-password", EMAIL));
        assertThat(user.getOtpAttempts()).isEqualTo(1);
    }

    @Test
    void codeIsLockedAfterTooManyAttempts() {
        withCode("123456", Instant.now());
        user.setOtpAttempts(AuthenticationService.MAX_OTP_ATTEMPTS);
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);

        assertThrows(UnauthorizedException.class,
            () -> authenticationService.redefinePassword("123456", "new-password", EMAIL));
        assertThat(user.getOtpCode()).isNull();
    }

    @Test
    void expiredCodeIsRejected() {
        withCode("123456", Instant.now().minus(6, ChronoUnit.MINUTES));
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);

        assertThrows(UnauthorizedException.class,
            () -> authenticationService.redefinePassword("123456", "new-password", EMAIL));
    }

    @Test
    void resetForUnknownEmailIsRejected() {
        when(userService.getUserByEmail(EMAIL)).thenThrow(new UserNotFoundException());

        assertThrows(UnauthorizedException.class,
            () -> authenticationService.redefinePassword("123456", "new-password", EMAIL));
    }

    @Test
    void correctCodeResetsPasswordAndClearsIt() {
        withCode("123456", Instant.now());
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);
        when(userService.encodePassword("new-password")).thenReturn("hashed");

        authenticationService.redefinePassword("123456", "new-password", EMAIL);

        assertThat(user.getPassword()).isEqualTo("hashed");
        assertThat(user.getOtpCode()).isNull();
        assertThat(user.getOtpTimestamp()).isNull();
    }

    private void withCode(String code, Instant createdAt) {
        user.setOtpCode(code);
        user.setOtpTimestamp(createdAt);
    }
}
