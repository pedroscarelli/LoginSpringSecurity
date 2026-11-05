package com.login.user.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinePasswordRequestDTO(
        @NotBlank @Pattern(regexp = "\\d{6}", message = "otpCode must have 6 digits") String otpCode,
        @NotBlank @Size(min = 6, max = 100) String newPassword,
        @NotBlank @Email String email
    ) { }
