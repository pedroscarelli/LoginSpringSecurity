package com.login.user.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.login.user.domain.model.User;
import com.login.user.domain.model.enums.UserRole;
import com.login.user.repository.UserRepository;
import com.login.user.service.TokenService;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityFilter securityFilter;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private MockFilterChain run(String authorization) throws Exception {
        var request = new MockHttpServletRequest();
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        var chain = new MockFilterChain();
        securityFilter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }

    @Test
    void validTokenAuthenticatesTheUser() throws Exception {
        var user = new User();
        user.setEmail("john@example.com");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        when(tokenService.validateToken("good")).thenReturn(Optional.of("john@example.com"));
        when(userRepository.findByEmail("john@example.com")).thenReturn(user);

        var chain = run("Bearer good");

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(user);
    }

    @Test
    void invalidTokenContinuesUnauthenticatedInsteadOfThrowing() throws Exception {
        when(tokenService.validateToken("expired")).thenReturn(Optional.empty());

        var chain = run("Bearer expired");

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void deletedUserIsNotAuthenticated() throws Exception {
        when(tokenService.validateToken("good")).thenReturn(Optional.of("gone@example.com"));
        when(userRepository.findByEmail("gone@example.com")).thenReturn(null);

        run("Bearer good");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void nonBearerHeaderIsIgnored() throws Exception {
        run("Basic dXNlcjpwYXNz");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenService, org.mockito.Mockito.never()).validateToken(org.mockito.ArgumentMatchers.anyString());
    }
}
