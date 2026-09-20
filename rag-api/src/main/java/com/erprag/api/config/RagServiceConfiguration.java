package com.erprag.api.config;

import com.erprag.core.RagService;
import com.erprag.core.RagServiceFactory;
import com.erprag.core.config.DbConfig;
import com.erprag.core.config.RagConfig;
import com.erprag.core.provider.ProviderSettings;
import com.erprag.core.provider.ProviderType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagServiceConfiguration {

    @Bean
    public RagService ragService(RagProperties properties) {
        DbConfig dbConfig = new DbConfig(
                properties.getDb().getHost(),
                properties.getDb().getPort(),
                properties.getDb().getDatabase(),
                properties.getDb().getUser(),
                properties.getDb().getPassword());

        ProviderSettings chatProvider = resolveChatProvider(properties);
        ProviderSettings embeddingProvider = resolveEmbeddingProvider(properties);

        RagConfig config = new RagConfig(
                dbConfig,
                chatProvider,
                embeddingProvider,
                properties.getStorage().getBasePath(),
                properties.getChunking().getMaxSegmentSizeChars(),
                properties.getChunking().getMaxOverlapChars(),
                properties.getRetrieval().getMaxResults(),
                properties.getRetrieval().getMinScore(),
                properties.getChatMemory().getMaxMessages());

        return RagServiceFactory.create(config);
    }

    private ProviderSettings resolveChatProvider(RagProperties properties) {
        ProviderType type = ProviderType.valueOf(properties.getLlm().getProvider().toUpperCase());
        return switch (type) {
            case OPENAI -> new ProviderSettings(type, properties.getLlm().getOpenai().getApiKey(),
                    properties.getLlm().getOpenai().getModel(), null, null);
            case ANTHROPIC -> new ProviderSettings(type, properties.getLlm().getAnthropic().getApiKey(),
                    properties.getLlm().getAnthropic().getModel(), null, null);
            case AZURE_OPENAI -> new ProviderSettings(type, properties.getLlm().getAzureOpenai().getApiKey(),
                    null, properties.getLlm().getAzureOpenai().getEndpoint(),
                    properties.getLlm().getAzureOpenai().getDeploymentName());
            case OLLAMA -> new ProviderSettings(type, null,
                    properties.getLlm().getOllama().getModel(), properties.getLlm().getOllama().getBaseUrl(), null);
        };
    }

    private ProviderSettings resolveEmbeddingProvider(RagProperties properties) {
        ProviderType type = ProviderType.valueOf(properties.getEmbedding().getProvider().toUpperCase());
        return switch (type) {
            case OPENAI -> new ProviderSettings(type, properties.getEmbedding().getOpenai().getApiKey(),
                    properties.getEmbedding().getOpenai().getModel(), null, null);
            case AZURE_OPENAI -> new ProviderSettings(type, properties.getEmbedding().getAzureOpenai().getApiKey(),
                    null, properties.getEmbedding().getAzureOpenai().getEndpoint(),
                    properties.getEmbedding().getAzureOpenai().getDeploymentName());
            case OLLAMA -> new ProviderSettings(type, null,
                    properties.getEmbedding().getOllama().getModel(), properties.getEmbedding().getOllama().getBaseUrl(), null);
            case ANTHROPIC -> throw new IllegalArgumentException("Anthropic cannot be used as the embedding provider");
        };
    }
}
