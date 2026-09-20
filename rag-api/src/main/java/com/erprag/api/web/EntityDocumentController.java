package com.erprag.api.web;

import com.erprag.api.security.SecurityUtils;
import com.erprag.api.web.dto.DocumentResponse;
import com.erprag.core.RagService;
import com.erprag.core.domain.DocumentRecord;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lets an entity's own ADMIN-role user upload documents directly (JWT-authenticated),
 * without needing the global admin API key. Mirrors {@link AdminDocumentController}'s
 * async upload pattern.
 */
@RestController
@RequestMapping("/api/entities/{entityCode}/documents")
public class EntityDocumentController {

    private final RagService ragService;
    private final ExecutorService ingestionExecutor;

    public EntityDocumentController(RagService ragService, ExecutorService ingestionExecutor) {
        this.ragService = ragService;
        this.ingestionExecutor = ingestionExecutor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DocumentResponse upload(@PathVariable("entityCode") String entityCode, @RequestParam("file") MultipartFile file) throws IOException {
        SecurityUtils.requireEntityAdmin(entityCode);
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        try (InputStream in = file.getInputStream()) {
            DocumentRecord initiated = ragService.initiateUpload(entityCode, filename, in, SecurityUtils.currentEntityUser().username());
            ingestionExecutor.submit(() -> ragService.processIngestion(entityCode, initiated.id()));
            return DocumentResponse.from(initiated);
        }
    }
}
