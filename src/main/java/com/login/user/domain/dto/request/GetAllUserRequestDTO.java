package com.login.user.domain.dto.request;

import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GetAllUserRequestDTO(
        @RequestParam(defaultValue = "1")
        @Min(value = 1, message = "Page must be greater than 0")
        int page,

        @RequestParam(defaultValue = "10")
        @Min(value = 1, message = "There must be at least 1 item per page")
        @Max(value = 100, message = "Maximum of 100 items per page")
        int items
        ) {}
