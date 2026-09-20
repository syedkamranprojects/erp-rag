package com.erprag.api.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String entityCode,
        @NotBlank String username,
        @NotBlank String password
) {
}
