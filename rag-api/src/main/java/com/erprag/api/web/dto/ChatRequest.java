package com.erprag.api.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String sessionId, @NotBlank String message) {
}
