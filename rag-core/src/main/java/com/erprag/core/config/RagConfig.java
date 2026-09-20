package com.erprag.core.config;

import com.erprag.core.provider.ProviderSettings;

/**
 * Everything needed to bootstrap a {@code RagService}: DB connection, chat/embedding
 * providers, local file storage root, chunking sizes and chat-memory window.
 */
public record RagConfig(
        DbConfig db,
        ProviderSettings chatProvider,
        ProviderSettings embeddingProvider,
        String storageBasePath,
        int maxSegmentSizeInChars,
        int maxOverlapSizeInChars,
        int maxRetrievedSegments,
        double minRetrievalScore,
        int maxChatMemoryMessages
) {
    public static RagConfig defaults(DbConfig db, ProviderSettings chatProvider, ProviderSettings embeddingProvider) {
        return new RagConfig(db, chatProvider, embeddingProvider, "./data/uploads", 1000, 100, 5, 0.6, 20);
    }
}
