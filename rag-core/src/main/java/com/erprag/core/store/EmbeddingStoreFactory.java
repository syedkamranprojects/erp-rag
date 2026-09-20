package com.erprag.core.store;

import com.erprag.core.config.DbConfig;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import java.util.List;

public final class EmbeddingStoreFactory {

    public static final String TABLE_NAME = "rag_embeddings";

    private EmbeddingStoreFactory() {
    }

    public static EmbeddingStore<TextSegment> create(DbConfig dbConfig, EmbeddingModel embeddingModel) {
        return PgVectorEmbeddingStore.builder()
                .host(dbConfig.host())
                .port(dbConfig.port())
                .database(dbConfig.database())
                .user(dbConfig.user())
                .password(dbConfig.password())
                .table(TABLE_NAME)
                .dimension(embeddingModel.dimension())
                .createTable(true)
                // No IVFFlat index for v1: it's an approximate index whose "lists" parameter must be
                // tuned to the actual row count (and pgvector's default probes=1 at query time isn't
                // configurable through this builder). At small-to-moderate scale that combination
                // silently drops true matches from the top-K results. Exact sequential-scan cosine
                // search is correct at any size and only becomes a real cost once a knowledge base
                // holds a very large number of chunks — revisit with a properly tuned index then.
                .useIndex(false)
                .metadataStorageConfig(DefaultMetadataStorageConfig.builder()
                        .storageMode(MetadataStorageMode.COMBINED_JSONB)
                        .columnDefinitions(List.of("metadata JSONB NULL"))
                        .build())
                .build();
    }
}
