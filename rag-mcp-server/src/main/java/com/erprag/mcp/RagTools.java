package com.erprag.mcp;

import com.erprag.core.RagService;
import com.erprag.core.domain.DocumentRecord;
import com.erprag.core.domain.KnowledgeBase;
import com.erprag.core.domain.KnowledgeBaseUser;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/** Builds the MCP tool specifications that expose {@link RagService} to an MCP client. */
final class RagTools {

    private final RagService ragService;
    private final McpJsonMapper jsonMapper;

    RagTools(RagService ragService, McpJsonMapper jsonMapper) {
        this.ragService = ragService;
        this.jsonMapper = jsonMapper;
    }

    List<McpServerFeatures.SyncToolSpecification> all() {
        return List.of(listEntities(), createEntity(), deleteEntity(), createEntityUser(),
                uploadDocument(), getDocumentStatus(), queryEntity());
    }

    private McpServerFeatures.SyncToolSpecification listEntities() {
        McpSchema.Tool tool = McpSchema.Tool.builder("list_entities", jsonMapper, """
                {"type":"object","properties":{},"additionalProperties":false}
                """)
                .description("List every entity (knowledge base) and how many documents each has.")
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    List<KnowledgeBase> all = ragService.listEntities();
                    StringBuilder sb = new StringBuilder();
                    for (KnowledgeBase kb : all) {
                        int docCount = ragService.listDocuments(kb.entityCode()).size();
                        sb.append(kb.entityCode()).append(" — ").append(kb.name())
                                .append(" (").append(docCount).append(" documents)\n");
                    }
                    return McpSchema.CallToolResult.builder()
                            .addTextContent(sb.isEmpty() ? "No entities yet." : sb.toString())
                            .build();
                })
                .build();
    }

    private McpServerFeatures.SyncToolSpecification createEntity() {
        McpSchema.Tool tool = McpSchema.Tool.builder("create_entity", jsonMapper, """
                {"type":"object","properties":{
                    "entityCode":{"type":"string","description":"Unique short code for the entity, e.g. 'acme'"},
                    "name":{"type":"string","description":"Human-readable display name"}
                },"required":["entityCode","name"],"additionalProperties":false}
                """)
                .description("Create a new entity (tenant) that documents can be uploaded against.")
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    String entityCode = stringArg(request, "entityCode");
                    String name = stringArg(request, "name");
                    KnowledgeBase kb = ragService.createEntity(entityCode, name);
                    return McpSchema.CallToolResult.builder()
                            .addTextContent("Created entity '" + kb.entityCode() + "' (" + kb.name() + ")")
                            .build();
                })
                .build();
    }

    private McpServerFeatures.SyncToolSpecification deleteEntity() {
        McpSchema.Tool tool = McpSchema.Tool.builder("delete_entity", jsonMapper, """
                {"type":"object","properties":{
                    "entityCode":{"type":"string"}
                },"required":["entityCode"],"additionalProperties":false}
                """)
                .description("Permanently delete an entity, its users, its documents, and their embeddings.")
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    String entityCode = stringArg(request, "entityCode");
                    ragService.deleteEntity(entityCode);
                    return McpSchema.CallToolResult.builder()
                            .addTextContent("Deleted entity '" + entityCode + "'")
                            .build();
                })
                .build();
    }

    private McpServerFeatures.SyncToolSpecification createEntityUser() {
        McpSchema.Tool tool = McpSchema.Tool.builder("create_entity_user", jsonMapper, """
                {"type":"object","properties":{
                    "entityCode":{"type":"string"},
                    "username":{"type":"string"},
                    "password":{"type":"string","description":"At least 8 characters"}
                },"required":["entityCode","username","password"],"additionalProperties":false}
                """)
                .description("Create a login for an entity, scoped only to that entity's documents.")
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    String entityCode = stringArg(request, "entityCode");
                    String username = stringArg(request, "username");
                    String password = stringArg(request, "password");
                    KnowledgeBaseUser user = ragService.createUser(entityCode, username, password);
                    return McpSchema.CallToolResult.builder()
                            .addTextContent("Created user '" + user.username() + "' for entity '" + entityCode + "'")
                            .build();
                })
                .build();
    }

    private McpServerFeatures.SyncToolSpecification uploadDocument() {
        McpSchema.Tool tool = McpSchema.Tool.builder("upload_document", jsonMapper, """
                {"type":"object","properties":{
                    "entityCode":{"type":"string"},
                    "filename":{"type":"string"},
                    "filePath":{"type":"string","description":"Absolute path to the file on disk (preferred for large files)"},
                    "fileContentBase64":{"type":"string","description":"Base64-encoded file content, used only if filePath is omitted"}
                },"required":["entityCode","filename"],"additionalProperties":false}
                """)
                .description("Upload a document (.docx/.pdf/.ppt/.pptx/.txt) to an entity: parses, chunks, embeds and stores it; blocks until indexing finishes.")
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    String entityCode = stringArg(request, "entityCode");
                    String filename = stringArg(request, "filename");
                    String filePath = optionalStringArg(request, "filePath");
                    String base64 = optionalStringArg(request, "fileContentBase64");

                    try (InputStream content = openContent(filePath, base64)) {
                        DocumentRecord record = ragService.uploadDocument(entityCode, filename, content, "mcp");
                        return McpSchema.CallToolResult.builder()
                                .addTextContent("Document '" + filename + "' -> " + record.status()
                                        + (record.chunkCount() != null ? " (" + record.chunkCount() + " chunks)" : "")
                                        + (record.errorMessage() != null ? " — " + record.errorMessage() : "")
                                        + " [id=" + record.id() + "]")
                                .build();
                    } catch (Exception e) {
                        return McpSchema.CallToolResult.builder()
                                .isError(true)
                                .addTextContent("Upload failed: " + e.getMessage())
                                .build();
                    }
                })
                .build();
    }

    private McpServerFeatures.SyncToolSpecification getDocumentStatus() {
        McpSchema.Tool tool = McpSchema.Tool.builder("get_document_status", jsonMapper, """
                {"type":"object","properties":{
                    "entityCode":{"type":"string"},
                    "documentId":{"type":"string"}
                },"required":["entityCode","documentId"],"additionalProperties":false}
                """)
                .description("Check the ingestion status of a previously uploaded document.")
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    String entityCode = stringArg(request, "entityCode");
                    UUID documentId = UUID.fromString(stringArg(request, "documentId"));
                    DocumentRecord record = ragService.getDocument(entityCode, documentId);
                    return McpSchema.CallToolResult.builder()
                            .addTextContent(record.originalFilename() + " -> " + record.status()
                                    + (record.chunkCount() != null ? " (" + record.chunkCount() + " chunks)" : "")
                                    + (record.errorMessage() != null ? " — " + record.errorMessage() : ""))
                            .build();
                })
                .build();
    }

    private McpServerFeatures.SyncToolSpecification queryEntity() {
        McpSchema.Tool tool = McpSchema.Tool.builder("query_entity", jsonMapper, """
                {"type":"object","properties":{
                    "entityCode":{"type":"string"},
                    "username":{"type":"string"},
                    "password":{"type":"string"},
                    "sessionId":{"type":"string","description":"Any stable id for this conversation, to keep chat memory"},
                    "message":{"type":"string"}
                },"required":["entityCode","username","password","sessionId","message"],"additionalProperties":false}
                """)
                .description("Ask a question, answered only from that entity's own uploaded documents. Requires that entity's own login credentials.")
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    String entityCode = stringArg(request, "entityCode");
                    String username = stringArg(request, "username");
                    String password = stringArg(request, "password");
                    String sessionId = stringArg(request, "sessionId");
                    String message = stringArg(request, "message");

                    try {
                        ragService.authenticate(entityCode, username, password);
                        String memoryId = entityCode + ":" + username + ":" + sessionId;
                        String answer = ragService.query(entityCode, memoryId, message);
                        return McpSchema.CallToolResult.builder().addTextContent(answer).build();
                    } catch (Exception e) {
                        return McpSchema.CallToolResult.builder()
                                .isError(true)
                                .addTextContent("Query failed: " + e.getMessage())
                                .build();
                    }
                })
                .build();
    }

    private static InputStream openContent(String filePath, String base64) throws Exception {
        if (filePath != null) {
            return new FileInputStream(Path.of(filePath).toFile());
        }
        if (base64 != null) {
            return new java.io.ByteArrayInputStream(Base64.getDecoder().decode(base64));
        }
        throw new IllegalArgumentException("Either filePath or fileContentBase64 must be provided");
    }

    private static String stringArg(McpSchema.CallToolRequest request, String key) {
        Object value = request.arguments().get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required argument '" + key + "'");
        }
        return value.toString();
    }

    private static String optionalStringArg(McpSchema.CallToolRequest request, String key) {
        Object value = request.arguments().get(key);
        return value == null ? null : value.toString();
    }
}
