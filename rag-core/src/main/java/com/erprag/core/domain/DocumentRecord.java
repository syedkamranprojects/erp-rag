package com.erprag.core.domain;

import java.time.Instant;
import java.util.UUID;

public record DocumentRecord(
        UUID id,
        Long knowledgeBaseId,
        String originalFilename,
        String contentType,
        String storagePath,
        DocumentStatus status,
        Integer chunkCount,
        String errorMessage,
        String uploadedBy,
        Instant uploadedAt,
        Instant indexedAt
) {
}
