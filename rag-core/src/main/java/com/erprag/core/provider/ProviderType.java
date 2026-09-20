package com.erprag.core.provider;

public enum ProviderType {
    OPENAI,
    ANTHROPIC,
    AZURE_OPENAI,
    OLLAMA,
    /** OpenAI-API-compatible endpoint at api.cerebras.ai; chat only, no embedding API. */
    CEREBRAS,
    /** OpenAI-API-compatible endpoint at api.groq.com; chat only, no embedding API. */
    GROQ
}
