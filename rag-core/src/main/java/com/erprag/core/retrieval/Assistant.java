package com.erprag.core.retrieval;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j AiServices target interface: one instance per entity, its retrieval
 * permanently scoped to that entity's own documents.
 */
public interface Assistant {

    @SystemMessage("""
            Answer the question using the information given to you below.
            If that information does not answer the question, say so plainly.
            """)
    String chat(@MemoryId String memoryId, @UserMessage String message);
}
