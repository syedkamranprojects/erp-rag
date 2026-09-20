package com.erprag.core.domain;

import java.time.Instant;

/**
 * The user-facing "Entity": a tenant boundary. Documents and users are scoped to one
 * KnowledgeBase, identified by its unique {@code entityCode}.
 */
public record KnowledgeBase(
        Long id,
        String entityCode,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
