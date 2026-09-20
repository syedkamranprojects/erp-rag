package com.erprag.api.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateEntityRequest(@NotBlank String name) {
}
