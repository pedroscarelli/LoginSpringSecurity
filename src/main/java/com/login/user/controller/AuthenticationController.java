package com.login.user.controller;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.login.user.domain.dto.request.*;
import com.login.user.service.*;

import io.swagger.v3.oas.annotations.enums.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;

@AllArgsConstructor
@RequestMapping(path = "/v1/auth", produces = "application/json")
@RestController
public class AuthenticationController {

    private AuthenticationService authenticationService;

    private TokenService tokenService;


    @PostMapping(path = "/login", consumes = "application/json")
    public ResponseEntity<@NonNull Map<String, String>> login(@RequestBody @Valid LoginRequestDTO loginRequestDto) {
        var authenticatedUser = authenticationService.authenticateLogin(loginRequestDto);
        var token = tokenService.generateToken(authenticatedUser);

        var response = Map.of("message", "Login successful", "jwtAuthenticationToken", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/redefine-password/activate", consumes = "application/json")
    public ResponseEntity<@NonNull Map<String, String>> activateUserRedefinePassword(@RequestBody @Valid EmailRequestDTO emailRequestDto) {
        authenticationService.activateRedefinePassword(emailRequestDto.email());
        var response = Map.of("message", "Password reset code sent");

        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/redefine-password", consumes = "application/json")
    public ResponseEntity<@NonNull Map<String, String>> redefinePassword(@RequestBody @Valid RedefinePasswordRequestDTO redefinePasswordRequestDto) {
        authenticationService.redefinePassword(
                redefinePasswordRequestDto.otpCode(),
                redefinePasswordRequestDto.newPassword(),
                redefinePasswordRequestDto.email()
            );
        var response = Map.of("message", "Password reset successfully");

        return ResponseEntity.ok(response);
    }
}
