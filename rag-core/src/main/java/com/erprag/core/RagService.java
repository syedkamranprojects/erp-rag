package com.erprag.core;

import com.erprag.core.auth.PasswordHasher;
import com.erprag.core.domain.DocumentRecord;
import com.erprag.core.domain.KnowledgeBase;
import com.erprag.core.domain.KnowledgeBaseUser;
import com.erprag.core.exception.AuthenticationException;
import com.erprag.core.exception.EntityAlreadyExistsException;
import com.erprag.core.exception.EntityNotFoundException;
import com.erprag.core.exception.DocumentNotFoundException;
import com.erprag.core.ingest.DocumentIngestionService;
import com.erprag.core.repo.DocumentRepository;
import com.erprag.core.repo.KnowledgeBaseRepository;
import com.erprag.core.repo.KnowledgeBaseUserRepository;
import com.erprag.core.retrieval.RagAssistantFactory;
import com.erprag.core.storage.FileStorage;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * The single reusable entry point to this RAG component. Both the REST API and the MCP
 * server are thin adapters that instantiate one of these (via {@link RagServiceFactory})
 * and delegate to it — any other Java-based AI application can do the same.
 */
public class RagService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final KnowledgeBaseUserRepository knowledgeBaseUserRepository;
    private final DocumentRepository documentRepository;
    private final DocumentIngestionService documentIngestionService;
    private final RagAssistantFactory ragAssistantFactory;
    private final FileStorage fileStorage;

    public RagService(KnowledgeBaseRepository knowledgeBaseRepository,
                       KnowledgeBaseUserRepository knowledgeBaseUserRepository,
                       DocumentRepository documentRepository,
                       DocumentIngestionService documentIngestionService,
                       RagAssistantFactory ragAssistantFactory,
                       FileStorage fileStorage) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.knowledgeBaseUserRepository = knowledgeBaseUserRepository;
        this.documentRepository = documentRepository;
        this.documentIngestionService = documentIngestionService;
        this.ragAssistantFactory = ragAssistantFactory;
        this.fileStorage = fileStorage;
    }

    // ---- Entity (KnowledgeBase) management ----

    public KnowledgeBase createEntity(String entityCode, String name) {
        if (knowledgeBaseRepository.findByEntityCode(entityCode).isPresent()) {
            throw new EntityAlreadyExistsException(entityCode);
        }
        return knowledgeBaseRepository.insert(entityCode, name);
    }

    public List<KnowledgeBase> listEntities() {
        return knowledgeBaseRepository.findAll();
    }

    public KnowledgeBase getEntity(String entityCode) {
        return knowledgeBaseRepository.findByEntityCode(entityCode)
                .orElseThrow(() -> new EntityNotFoundException(entityCode));
    }

    public KnowledgeBase updateEntity(String entityCode, String newName) {
        return knowledgeBaseRepository.updateName(entityCode, newName)
                .orElseThrow(() -> new EntityNotFoundException(entityCode));
    }

    public void deleteEntity(String entityCode) {
        boolean deleted = knowledgeBaseRepository.deleteByEntityCode(entityCode);
        if (!deleted) {
            throw new EntityNotFoundException(entityCode);
        }
    }

    // ---- Entity-scoped users ----

    public KnowledgeBaseUser createUser(String entityCode, String username, String rawPassword) {
        KnowledgeBase kb = getEntity(entityCode);
        String hash = PasswordHasher.hash(rawPassword);
        return knowledgeBaseUserRepository.insert(kb.id(), username, hash, "MEMBER");
    }

    public KnowledgeBaseUser authenticate(String entityCode, String username, String rawPassword) {
        KnowledgeBase kb = getEntity(entityCode);
        KnowledgeBaseUser user = knowledgeBaseUserRepository.findByKnowledgeBaseAndUsername(kb.id(), username)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        if (!PasswordHasher.matches(rawPassword, user.passwordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }
        return user;
    }

    // ---- Documents ----

    /**
     * Saves the raw file to disk and inserts a {@code PROCESSING} document row. Fast and
     * I/O-only — callers that want non-blocking uploads (e.g. the REST/MCP adapters) call
     * this synchronously, return the resulting record to the client, then run
     * {@link #processIngestion} on a background executor.
     */
    public DocumentRecord initiateUpload(String entityCode, String filename, InputStream content, String uploadedBy) {
        KnowledgeBase kb = getEntity(entityCode);
        UUID documentId = UUID.randomUUID();
        Path storagePath = fileStorage.save(entityCode, documentId, filename, content);
        return documentRepository.insert(documentId, kb.id(), filename, null, storagePath.toString(), uploadedBy);
    }

    /**
     * The heavy step (parse/chunk/embed/store) for a document already saved via
     * {@link #initiateUpload}. Updates the document's status to {@code INDEXED} or
     * {@code FAILED} when done.
     */
    public void processIngestion(String entityCode, UUID documentId) {
        DocumentRecord record = getDocument(entityCode, documentId);
        try (InputStream in = fileStorage.open(Path.of(record.storagePath()))) {
            int chunkCount = documentIngestionService.ingest(entityCode, documentId, record.originalFilename(), in);
            documentRepository.markIndexed(documentId, chunkCount);
        } catch (Exception e) {
            documentRepository.markFailed(documentId, e.getMessage());
        }
    }

    /**
     * Convenience for callers that are fine blocking until ingestion finishes
     * (e.g. a synchronous MCP tool call).
     */
    public DocumentRecord uploadDocument(String entityCode, String filename, InputStream content, String uploadedBy) {
        DocumentRecord initiated = initiateUpload(entityCode, filename, content, uploadedBy);
        processIngestion(entityCode, initiated.id());
        return documentRepository.findById(initiated.id()).orElseThrow();
    }

    public DocumentRecord getDocument(String entityCode, UUID documentId) {
        DocumentRecord record = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        KnowledgeBase kb = getEntity(entityCode);
        if (!record.knowledgeBaseId().equals(kb.id())) {
            throw new DocumentNotFoundException(documentId);
        }
        return record;
    }

    public List<DocumentRecord> listDocuments(String entityCode) {
        KnowledgeBase kb = getEntity(entityCode);
        return documentRepository.findByKnowledgeBase(kb.id());
    }

    // ---- Chat ----

    public String query(String entityCode, String memoryId, String message) {
        getEntity(entityCode); // validates the entity exists before ever touching the assistant cache
        return ragAssistantFactory.forEntity(entityCode).chat(memoryId, message);
    }
}
