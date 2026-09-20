package com.erprag.api.web.dto;

import com.erprag.core.domain.DocumentRecord;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String filename,
        String status,
        Integer chunkCount,
        String errorMessage,
        Instant uploadedAt,
        Instant indexedAt
) {
    public static DocumentResponse from(DocumentRecord record) {
        return new DocumentResponse(
                record.id(),
                record.originalFilename(),
                record.status().name(),
                record.chunkCount(),
                record.errorMessage(),
                record.uploadedAt(),
                record.indexedAt());
    }
}
