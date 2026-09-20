package com.erprag.api.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateEntityRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,64}", message = "entityCode must be alphanumeric (with _ or -), max 64 chars")
        String entityCode,
        @NotBlank String name
) {
}
