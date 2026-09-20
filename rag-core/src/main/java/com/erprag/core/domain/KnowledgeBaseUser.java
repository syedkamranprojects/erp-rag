package com.erprag.core.domain;

import java.time.Instant;

/**
 * A login scoped to one KnowledgeBase. {@code username} is unique only within its
 * owning KnowledgeBase (composite unique constraint), not globally.
 */
public record KnowledgeBaseUser(
        Long id,
        Long knowledgeBaseId,
        String username,
        String passwordHash,
        String role,
        Instant createdAt
) {
}
