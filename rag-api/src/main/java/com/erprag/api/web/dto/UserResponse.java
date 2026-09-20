package com.erprag.api.web.dto;

import com.erprag.core.domain.KnowledgeBaseUser;

public record UserResponse(String username, String role) {
    public static UserResponse from(KnowledgeBaseUser user) {
        return new UserResponse(user.username(), user.role());
    }
}
