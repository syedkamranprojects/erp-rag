package com.erprag.mcp;

import com.erprag.core.RagService;
import com.erprag.core.RagServiceFactory;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exposes {@link RagService} — the same reusable RAG component the REST API adapts — as
 * an MCP server over stdio, so Claude Desktop/Claude Code can call it directly as a tool.
 * Admin-shaped tools (entity/user/document management) rely on this whole process having
 * been launched with admin rights (gated by requiring RAG_MCP_ADMIN_API_KEY to be set at
 * startup); query_entity always requires its own entity credentials per call regardless.
 */
public final class RagMcpServer {

    private static final Logger log = LoggerFactory.getLogger(RagMcpServer.class);

    public static void main(String[] args) throws InterruptedException {
        EnvConfig.adminApiKey(); // fail fast if this process wasn't launched with admin rights

        RagService ragService = RagServiceFactory.create(EnvConfig.loadRagConfig());

        McpJsonMapper jsonMapper = new JacksonMcpJsonMapper(JsonMapper.shared());
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(jsonMapper);

        RagTools tools = new RagTools(ragService, jsonMapper);

        McpSyncServer server = McpServer.sync(transportProvider)
                .serverInfo("erp-rag-mcp-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(tools.all())
                .build();

        log.info("erp-rag MCP server started over stdio with {} tools", tools.all().size());

        CountDownLatch shutdownLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.closeGracefully();
            shutdownLatch.countDown();
        }));
        shutdownLatch.await();
    }
}
