package com.erprag.core.provider;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import java.time.Duration;

/**
 * Builds a {@link ChatModel} or {@link EmbeddingModel} from a {@link ProviderSettings},
 * so switching LLM/embedding vendors is a config change, never a recompile.
 */
public final class ModelProviderFactory {

    /** Ollama serving a model on CPU can legitimately take minutes; LangChain4j's default is too short for that. */
    private static final Duration OLLAMA_TIMEOUT = Duration.ofMinutes(5);

    private ModelProviderFactory() {
    }

    public static ChatModel chatModel(ProviderSettings settings) {
        return switch (settings.type()) {
            case OPENAI -> OpenAiChatModel.builder()
                    .apiKey(settings.apiKey())
                    .modelName(settings.modelName())
                    .build();
            case ANTHROPIC -> AnthropicChatModel.builder()
                    .apiKey(settings.apiKey())
                    .modelName(settings.modelName())
                    .build();
            case AZURE_OPENAI -> AzureOpenAiChatModel.builder()
                    .endpoint(settings.baseUrl())
                    .apiKey(settings.apiKey())
                    .deploymentName(settings.deploymentName())
                    .build();
            case OLLAMA -> OllamaChatModel.builder()
                    .baseUrl(settings.baseUrl())
                    .modelName(settings.modelName())
                    .timeout(OLLAMA_TIMEOUT)
                    .build();
            // Cerebras and Groq both expose an OpenAI-compatible chat completions API,
            // so no separate LangChain4j integration module is needed for either.
            case CEREBRAS, GROQ -> OpenAiChatModel.builder()
                    .baseUrl(settings.baseUrl())
                    .apiKey(settings.apiKey())
                    .modelName(settings.modelName())
                    .build();
        };
    }

    public static EmbeddingModel embeddingModel(ProviderSettings settings) {
        return switch (settings.type()) {
            case OPENAI -> OpenAiEmbeddingModel.builder()
                    .apiKey(settings.apiKey())
                    .modelName(settings.modelName())
                    .build();
            case AZURE_OPENAI -> AzureOpenAiEmbeddingModel.builder()
                    .endpoint(settings.baseUrl())
                    .apiKey(settings.apiKey())
                    .deploymentName(settings.deploymentName())
                    .build();
            case OLLAMA -> OllamaEmbeddingModel.builder()
                    .baseUrl(settings.baseUrl())
                    .modelName(settings.modelName())
                    .timeout(OLLAMA_TIMEOUT)
                    .build();
            case ANTHROPIC -> throw new IllegalArgumentException(
                    "Anthropic has no embedding API; configure a different embedding provider (e.g. OpenAI or Ollama)");
            case CEREBRAS, GROQ -> throw new IllegalArgumentException(
                    settings.type() + " has no embedding API; configure a different embedding provider (e.g. OpenAI or Ollama)");
        };
    }
}
