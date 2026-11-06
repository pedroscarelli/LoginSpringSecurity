package com.login.user.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

class SecureTokensTest {

    @Test
    void otpCodeAlwaysHasSixDigits() {
        for (int i = 0; i < 1_000; i++) {
            assertThat(SecureTokens.otpCode()).matches("\\d{6}");
        }
    }

    @Test
    void activationTokensAreUrlSafeAndUnique() {
        var tokens = new HashSet<String>();
        for (int i = 0; i < 1_000; i++) {
            var token = SecureTokens.activationToken();
            assertThat(token).matches("[A-Za-z0-9_-]{43}");
            tokens.add(token);
        }
        assertThat(tokens).hasSize(1_000);
    }

    @Test
    void constantTimeEquals() {
        assertThat(SecureTokens.constantTimeEquals("123456", "123456")).isTrue();
        assertThat(SecureTokens.constantTimeEquals("123456", "123457")).isFalse();
        assertThat(SecureTokens.constantTimeEquals("123456", null)).isFalse();
        assertThat(SecureTokens.constantTimeEquals(null, null)).isFalse();
    }
}
