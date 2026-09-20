package com.erprag.core.retrieval;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds and caches one {@link Assistant} per entity code. Each assistant's
 * {@link ContentRetriever} has an {@code entityCode} filter fixed at construction time —
 * the strongest possible tenant-isolation guarantee, since there is no per-request filter
 * that could be forgotten or bypassed.
 */
public class RagAssistantFactory {

    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final int maxRetrievedSegments;
    private final double minRetrievalScore;
    private final int maxChatMemoryMessages;
    private final ConcurrentHashMap<String, Assistant> assistantsByEntityCode = new ConcurrentHashMap<>();

    public RagAssistantFactory(ChatModel chatModel,
                                EmbeddingModel embeddingModel,
                                EmbeddingStore<TextSegment> embeddingStore,
                                int maxRetrievedSegments,
                                double minRetrievalScore,
                                int maxChatMemoryMessages) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.maxRetrievedSegments = maxRetrievedSegments;
        this.minRetrievalScore = minRetrievalScore;
        this.maxChatMemoryMessages = maxChatMemoryMessages;
    }

    public Assistant forEntity(String entityCode) {
        return assistantsByEntityCode.computeIfAbsent(entityCode, this::buildAssistant);
    }

    private Assistant buildAssistant(String entityCode) {
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxRetrievedSegments)
                .minScore(minRetrievalScore)
                .filter(MetadataFilterBuilder.metadataKey("entityCode").isEqualTo(entityCode))
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(CompressingQueryTransformer.builder()
                        .chatModel(chatModel)
                        .build())
                .contentRetriever(contentRetriever)
                .build();

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(maxChatMemoryMessages)
                        .build())
                .build();
    }
}
