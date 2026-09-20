package com.erprag.mcp;

import com.erprag.core.config.DbConfig;
import com.erprag.core.config.RagConfig;
import com.erprag.core.provider.ProviderSettings;
import com.erprag.core.provider.ProviderType;

/** Reads the same environment variables rag-api uses, so both adapters can be configured identically. */
final class EnvConfig {

    private EnvConfig() {
    }

    static String adminApiKey() {
        return require("RAG_MCP_ADMIN_API_KEY");
    }

    static RagConfig loadRagConfig() {
        DbConfig db = new DbConfig(
                env("PGVECTOR_HOST", "localhost"),
                Integer.parseInt(env("PGVECTOR_PORT", "5432")),
                env("PGVECTOR_DB", "ragdb"),
                env("PGVECTOR_USER", "rag"),
                env("PGVECTOR_PASSWORD", "rag"));

        ProviderType chatType = ProviderType.valueOf(env("RAG_LLM_PROVIDER", "openai").toUpperCase());
        ProviderSettings chatProvider = switch (chatType) {
            case OPENAI -> new ProviderSettings(chatType, env("OPENAI_API_KEY", ""), env("RAG_LLM_MODEL", "gpt-4o-mini"), null, null);
            case ANTHROPIC -> new ProviderSettings(chatType, env("ANTHROPIC_API_KEY", ""), env("RAG_LLM_MODEL", "claude-sonnet-4-5"), null, null);
            case AZURE_OPENAI -> new ProviderSettings(chatType, env("AZURE_OPENAI_API_KEY", ""), null, env("AZURE_OPENAI_ENDPOINT", ""), env("AZURE_OPENAI_CHAT_DEPLOYMENT", ""));
            case OLLAMA -> new ProviderSettings(chatType, null, env("RAG_LLM_MODEL", "llama3.1"), env("OLLAMA_BASE_URL", "http://localhost:11434"), null);
            case CEREBRAS -> new ProviderSettings(chatType, env("CEREBRAS_API_KEY", ""), env("RAG_LLM_MODEL", "llama-4-scout-17b-16e-instruct"), env("CEREBRAS_BASE_URL", "https://api.cerebras.ai/v1"), null);
            case GROQ -> new ProviderSettings(chatType, env("GROQ_API_KEY", ""), env("RAG_LLM_MODEL", "openai/gpt-oss-120b"), env("GROQ_BASE_URL", "https://api.groq.com/openai/v1"), null);
        };

        ProviderType embeddingType = ProviderType.valueOf(env("RAG_EMBEDDING_PROVIDER", "openai").toUpperCase());
        ProviderSettings embeddingProvider = switch (embeddingType) {
            case OPENAI -> new ProviderSettings(embeddingType, env("OPENAI_API_KEY", ""), env("RAG_EMBEDDING_MODEL", "text-embedding-3-small"), null, null);
            case AZURE_OPENAI -> new ProviderSettings(embeddingType, env("AZURE_OPENAI_API_KEY", ""), null, env("AZURE_OPENAI_ENDPOINT", ""), env("AZURE_OPENAI_EMBEDDING_DEPLOYMENT", ""));
            case OLLAMA -> new ProviderSettings(embeddingType, null, env("RAG_EMBEDDING_MODEL", "nomic-embed-text"), env("OLLAMA_BASE_URL", "http://localhost:11434"), null);
            case ANTHROPIC -> throw new IllegalArgumentException("Anthropic cannot be used as the embedding provider");
            case CEREBRAS -> throw new IllegalArgumentException("Cerebras cannot be used as the embedding provider");
            case GROQ -> throw new IllegalArgumentException("Groq cannot be used as the embedding provider");
        };

        return new RagConfig(
                db,
                chatProvider,
                embeddingProvider,
                env("RAG_STORAGE_PATH", "./data/uploads"),
                Integer.parseInt(env("RAG_CHUNK_SIZE", "1000")),
                Integer.parseInt(env("RAG_CHUNK_OVERLAP", "100")),
                Integer.parseInt(env("RAG_MAX_RESULTS", "5")),
                Double.parseDouble(env("RAG_MIN_SCORE", "0.6")),
                Integer.parseInt(env("RAG_CHAT_MEMORY_MAX_MESSAGES", "20")));
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set — this MCP server carries admin rights and refuses to start without it");
        }
        return value;
    }
}
