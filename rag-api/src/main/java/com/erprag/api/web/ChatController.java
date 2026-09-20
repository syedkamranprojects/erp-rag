package com.erprag.api.web;

import com.erprag.api.security.EntityUserPrincipal;
import com.erprag.api.security.SecurityUtils;
import com.erprag.api.web.dto.ChatRequest;
import com.erprag.api.web.dto.ChatResponse;
import com.erprag.api.web.dto.DocumentResponse;
import com.erprag.core.RagService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Entity-scoped endpoints for logged-in entity users: chat, and read-only document status. */
@RestController
@RequestMapping("/api/entities/{entityCode}")
public class ChatController {

    private final RagService ragService;

    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@PathVariable("entityCode") String entityCode, @Valid @RequestBody ChatRequest request) {
        SecurityUtils.requireEntityMatch(entityCode);
        EntityUserPrincipal principal = SecurityUtils.currentEntityUser();
        String memoryId = entityCode + ":" + principal.username() + ":" + request.sessionId();
        String answer = ragService.query(entityCode, memoryId, request.message());
        return new ChatResponse(answer);
    }

    @GetMapping("/documents")
    public List<DocumentResponse> listDocuments(@PathVariable("entityCode") String entityCode) {
        SecurityUtils.requireEntityMatch(entityCode);
        return ragService.listDocuments(entityCode).stream().map(DocumentResponse::from).toList();
    }

    @GetMapping("/documents/{documentId}")
    public DocumentResponse documentStatus(@PathVariable("entityCode") String entityCode, @PathVariable("documentId") UUID documentId) {
        SecurityUtils.requireEntityMatch(entityCode);
        return DocumentResponse.from(ragService.getDocument(entityCode, documentId));
    }
}
