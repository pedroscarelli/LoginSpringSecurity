package com.login.user.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.login.user.domain.exception.ForbiddenException;
import com.login.user.domain.model.User;
import com.login.user.domain.model.enums.UserRole;

class ValidationUtilsTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private UUID loginAs(UserRole role) {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(role);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        return user.getId();
    }

    @Test
    void userCanAccessOwnAccount() {
        var id = loginAs(UserRole.USER);
        assertDoesNotThrow(() -> ValidationUtils.isTargetUserSameFromRequest(id));
    }

    @Test
    void userCannotAccessAnotherAccount() {
        loginAs(UserRole.USER);
        assertThrows(ForbiddenException.class, () -> ValidationUtils.isTargetUserSameFromRequest(UUID.randomUUID()));
    }

    @Test
    void adminCanAccessAnyAccount() {
        loginAs(UserRole.ADMIN);
        assertDoesNotThrow(() -> ValidationUtils.isTargetUserSameFromRequest(UUID.randomUUID()));
    }
}
