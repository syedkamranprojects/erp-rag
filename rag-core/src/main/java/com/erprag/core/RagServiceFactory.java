package com.erprag.core;

import com.erprag.core.config.RagConfig;
import com.erprag.core.db.DataSourceFactory;
import com.erprag.core.db.FlywayBootstrap;
import com.erprag.core.ingest.DocumentIngestionService;
import com.erprag.core.provider.ModelProviderFactory;
import com.erprag.core.repo.DocumentRepository;
import com.erprag.core.repo.KnowledgeBaseRepository;
import com.erprag.core.repo.KnowledgeBaseUserRepository;
import com.erprag.core.retrieval.RagAssistantFactory;
import com.erprag.core.storage.FileStorage;
import com.erprag.core.store.EmbeddingStoreFactory;
import com.zaxxer.hikari.HikariDataSource;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

/**
 * Bootstraps a fully-wired {@link RagService} from a {@link RagConfig}: runs Flyway
 * migrations, builds the chat/embedding models, the pgvector store, and every repository.
 * This is the one entry point any Java application (a Spring app, an MCP server, a plain
 * CLI, or another AI framework's tool layer) needs to embed this RAG component.
 */
public final class RagServiceFactory {

    private RagServiceFactory() {
    }

    public static RagService create(RagConfig config) {
        HikariDataSource dataSource = DataSourceFactory.create(config.db());
        FlywayBootstrap.migrate(dataSource);

        ChatModel chatModel = ModelProviderFactory.chatModel(config.chatProvider());
        EmbeddingModel embeddingModel = ModelProviderFactory.embeddingModel(config.embeddingProvider());
        EmbeddingStore<TextSegment> embeddingStore = EmbeddingStoreFactory.create(config.db(), embeddingModel);

        DocumentIngestionService ingestionService = new DocumentIngestionService(
                embeddingModel, embeddingStore, config.maxSegmentSizeInChars(), config.maxOverlapSizeInChars());

        RagAssistantFactory assistantFactory = new RagAssistantFactory(
                chatModel, embeddingModel, embeddingStore,
                config.maxRetrievedSegments(), config.minRetrievalScore(), config.maxChatMemoryMessages());

        return new RagService(
                new KnowledgeBaseRepository(dataSource),
                new KnowledgeBaseUserRepository(dataSource),
                new DocumentRepository(dataSource),
                ingestionService,
                assistantFactory,
                new FileStorage(config.storageBasePath()));
    }
}
