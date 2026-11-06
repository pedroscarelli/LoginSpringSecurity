package com.login.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.login.user.domain.dto.request.CreateUserRequestDTO;
import com.login.user.domain.dto.request.UpdateUserRequestDTO;
import com.login.user.domain.exception.UserNotFoundException;
import com.login.user.domain.mapper.UserMapper;
import com.login.user.domain.model.User;
import com.login.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceUpdateAndActivationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser(UUID id) {
        var user = new User();
        user.setId(id);
        user.setName("John");
        user.setPassword("old-hash");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void updatingOnlyTheNameKeepsThePassword() {
        var id = UUID.randomUUID();
        existingUser(id);

        var updated = userService.updateUser(id, new UpdateUserRequestDTO("Johnny", null));

        assertThat(updated.getName()).isEqualTo("Johnny");
        assertThat(updated.getPassword()).isEqualTo("old-hash");
    }

    @Test
    void updatingOnlyThePasswordKeepsTheNameAndHashesIt() {
        var id = UUID.randomUUID();
        existingUser(id);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        var updated = userService.updateUser(id, new UpdateUserRequestDTO(null, "new-password"));

        assertThat(updated.getName()).isEqualTo("John");
        assertThat(updated.getPassword()).isEqualTo("new-hash");
    }

    @Test
    void newUserGetsARandomActivationTokenInsteadOfItsId() {
        when(passwordEncoder.encode("password")).thenReturn("hash");

        var user = userService.createUser(new CreateUserRequestDTO("John", "john@example.com", "password"));

        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getPassword()).isEqualTo("hash");
        assertThat(user.getActivationToken()).hasSizeGreaterThanOrEqualTo(40);
        verify(emailService).sendSignUpEmail("john@example.com", user.getActivationToken());
    }

    @Test
    void activationEnablesTheUserAndConsumesTheToken() {
        var user = new User();
        user.setActivationToken("token");
        when(userRepository.findByActivationToken("token")).thenReturn(Optional.of(user));

        userService.activateUser("token");

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getActivationToken()).isNull();
    }

    @Test
    void unknownActivationTokenIsRejected() {
        when(userRepository.findByActivationToken("nope")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.activateUser("nope"));
    }
}
