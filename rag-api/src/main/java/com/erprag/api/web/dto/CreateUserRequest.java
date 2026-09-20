package com.erprag.api.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
        @Pattern(regexp = "ADMIN|MEMBER", message = "role must be 'ADMIN' or 'MEMBER'") String role
) {
    /** Defaults to MEMBER when the caller omits a role entirely. */
    public String roleOrDefault() {
        return role == null || role.isBlank() ? "MEMBER" : role;
    }
}
