package com.login.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "api.security.token")
public record TokenServiceConfig (
    @NotBlank @Size(min = 32, message = "JWT_SECRET must have at least 32 characters") String jwtSecret,
    @NotNull @Positive Long jwtExpirationInSeconds,
    @NotBlank String jwtIssuer
) {}
