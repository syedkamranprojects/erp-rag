package com.erprag.api.web;

import com.erprag.core.RagService;
import com.erprag.core.domain.DocumentRecord;
import com.erprag.api.web.dto.DocumentResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/entities/{entityCode}/documents")
public class AdminDocumentController {

    private final RagService ragService;
    private final ExecutorService ingestionExecutor;

    public AdminDocumentController(RagService ragService, ExecutorService ingestionExecutor) {
        this.ragService = ragService;
        this.ingestionExecutor = ingestionExecutor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DocumentResponse upload(@PathVariable("entityCode") String entityCode, @RequestParam("file") MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        try (InputStream in = file.getInputStream()) {
            DocumentRecord initiated = ragService.initiateUpload(entityCode, filename, in, "admin");
            ingestionExecutor.submit(() -> ragService.processIngestion(entityCode, initiated.id()));
            return DocumentResponse.from(initiated);
        }
    }

    @GetMapping
    public List<DocumentResponse> list(@PathVariable("entityCode") String entityCode) {
        return ragService.listDocuments(entityCode).stream().map(DocumentResponse::from).toList();
    }

    @GetMapping("/{documentId}")
    public DocumentResponse status(@PathVariable("entityCode") String entityCode, @PathVariable("documentId") UUID documentId) {
        return DocumentResponse.from(ragService.getDocument(entityCode, documentId));
    }
}
