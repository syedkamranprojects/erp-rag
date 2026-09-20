package com.erprag.api.web.dto;

import com.erprag.core.domain.KnowledgeBase;
import java.time.Instant;

public record EntityResponse(String entityCode, String name, Instant createdAt, Instant updatedAt) {
    public static EntityResponse from(KnowledgeBase kb) {
        return new EntityResponse(kb.entityCode(), kb.name(), kb.createdAt(), kb.updatedAt());
    }
}
