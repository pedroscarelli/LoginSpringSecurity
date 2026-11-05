package com.login.user.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.login.user.domain.dto.request.CreateUserRequestDTO;
import com.login.user.domain.dto.request.UpdateUserRequestDTO;
import com.login.user.domain.dto.response.UserPaginationResponseDTO;
import com.login.user.domain.exception.DuplicateCredentialsException;
import com.login.user.domain.exception.UserNotFoundException;
import com.login.user.domain.mapper.UserMapper;
import com.login.user.domain.model.User;
import com.login.user.domain.model.enums.UserRole;
import com.login.user.repository.UserRepository;
import com.login.user.util.SecureTokens;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public UserPaginationResponseDTO getAllUsers(int page, int items) {
        var users = userRepository.findAll(PageRequest.of(page - 1, items));
        var usersResponseDto = users.getContent().stream()
            .map(userMapper::toDto)
            .toList();

        return new UserPaginationResponseDTO(
            users.getTotalElements(),
            users.getTotalPages(),
            usersResponseDto
        );
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public void save(User userToSave) {
        userRepository.save(userToSave);
    }

    public User getUserByEmail(String email) {
        var userFound = userRepository.findByEmail(email);
        if (userFound == null) {
            throw new UserNotFoundException();
        }

        return userFound;
    }

    public User createUser(CreateUserRequestDTO createUserRequestDto) {
        isUserCredentialsDuplicated(createUserRequestDto.email());

        var newUser = new User();
        newUser.setName(createUserRequestDto.name());
        newUser.setEmail(createUserRequestDto.email());
        newUser.setPassword(encodePassword(createUserRequestDto.password()));
        newUser.setRole(UserRole.USER);
        newUser.setEnabled(false);
        newUser.setActivationToken(SecureTokens.activationToken());
        userRepository.save(newUser);

        emailService.sendSignUpEmail(newUser.getEmail(), newUser.getActivationToken());

        return newUser;
    }

    public void isUserCredentialsDuplicated(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateCredentialsException();
        }
    }

    /** Applies only the fields that were sent; anything left null keeps its current value. */
    public User updateUser(UUID id, UpdateUserRequestDTO updateUserRequestDto) {
        var userToUpdate = getUserById(id);

        if (updateUserRequestDto.name() != null) {
            userToUpdate.setName(updateUserRequestDto.name());
        }

        if (updateUserRequestDto.password() != null) {
            userToUpdate.setPassword(encodePassword(updateUserRequestDto.password()));
        }

        userRepository.save(userToUpdate);

        return userToUpdate;
    }

    public User deleteUser(UUID id) {
        var userToDelete = getUserById(id);
        userToDelete.setDeletedAt(Instant.now());
        userRepository.save(userToDelete);

        return userToDelete;
    }

    public void activateUser(String activationToken) {
        var userToActivate = userRepository.findByActivationToken(activationToken)
            .orElseThrow(UserNotFoundException::new);

        userToActivate.setEnabled(true);
        userToActivate.setActivationToken(null);
        userRepository.save(userToActivate);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(user.getAuthorities())
            .build();
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
