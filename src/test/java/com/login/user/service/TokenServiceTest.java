package com.login.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.login.user.config.TokenServiceConfig;

class TokenServiceTest {

    private static final String SECRET = "a-test-secret-that-is-long-enough-123";

    private final TokenService tokenService = new TokenService(new TokenServiceConfig(SECRET, 1800L, "login-api"));

    private final User user = new User("john@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    void validTokenReturnsTheSubject() {
        var token = tokenService.generateToken(user);

        assertThat(tokenService.validateToken(token)).contains("john@example.com");
    }

    @Test
    void expirationUsesTheConfiguredSeconds() {
        var before = Instant.now();
        var expiresAt = JWT.decode(tokenService.generateToken(user)).getExpiresAtAsInstant();

        assertThat(expiresAt).isBetween(
            before.plusSeconds(1800).minusSeconds(2),
            Instant.now().plusSeconds(1800).plusSeconds(2));
    }

    @Test
    void tamperedTokenIsRejected() {
        var token = tokenService.generateToken(user);

        assertThat(tokenService.validateToken(token + "x")).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        var other = new TokenService(new TokenServiceConfig("another-secret-that-is-long-enough-456", 1800L, "login-api"));

        assertThat(tokenService.validateToken(other.generateToken(user))).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        var expired = JWT.create()
            .withIssuer("login-api")
            .withSubject("john@example.com")
            .withExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
            .sign(Algorithm.HMAC256(SECRET));

        assertThat(tokenService.validateToken(expired)).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        assertThat(tokenService.validateToken("not-a-token")).isEmpty();
    }
}
