package com.erprag.core.repo;

import com.erprag.core.domain.DocumentRecord;
import com.erprag.core.domain.DocumentStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

public class DocumentRepository {

    private final DataSource dataSource;

    public DocumentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DocumentRecord insert(UUID id, long knowledgeBaseId, String originalFilename, String contentType,
                                  String storagePath, String uploadedBy) {
        String sql = """
                INSERT INTO document (id, knowledge_base_id, original_filename, content_type, storage_path, status, uploaded_by)
                VALUES (?, ?, ?, ?, ?, 'PROCESSING', ?)
                RETURNING id, knowledge_base_id, original_filename, content_type, storage_path, status,
                          chunk_count, error_message, uploaded_by, uploaded_at, indexed_at
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setLong(2, knowledgeBaseId);
            ps.setString(3, originalFilename);
            ps.setString(4, contentType);
            ps.setString(5, storagePath);
            ps.setString(6, uploadedBy);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert document", e);
        }
    }

    public void markIndexed(UUID id, int chunkCount) {
        String sql = """
                UPDATE document SET status = 'INDEXED', chunk_count = ?, indexed_at = now(), error_message = NULL
                WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chunkCount);
            ps.setObject(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark document indexed", e);
        }
    }

    public void markFailed(UUID id, String errorMessage) {
        String sql = "UPDATE document SET status = 'FAILED', error_message = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, errorMessage);
            ps.setObject(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark document failed", e);
        }
    }

    public Optional<DocumentRecord> findById(UUID id) {
        String sql = """
                SELECT id, knowledge_base_id, original_filename, content_type, storage_path, status,
                       chunk_count, error_message, uploaded_by, uploaded_at, indexed_at
                FROM document WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up document", e);
        }
    }

    public List<DocumentRecord> findByKnowledgeBase(long knowledgeBaseId) {
        String sql = """
                SELECT id, knowledge_base_id, original_filename, content_type, storage_path, status,
                       chunk_count, error_message, uploaded_by, uploaded_at, indexed_at
                FROM document WHERE knowledge_base_id = ? ORDER BY uploaded_at
                """;
        List<DocumentRecord> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, knowledgeBaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list documents", e);
        }
    }

    private DocumentRecord map(ResultSet rs) throws SQLException {
        Integer chunkCount = (Integer) rs.getObject("chunk_count");
        return new DocumentRecord(
                (UUID) rs.getObject("id"),
                rs.getLong("knowledge_base_id"),
                rs.getString("original_filename"),
                rs.getString("content_type"),
                rs.getString("storage_path"),
                DocumentStatus.valueOf(rs.getString("status")),
                chunkCount,
                rs.getString("error_message"),
                rs.getString("uploaded_by"),
                toInstant(rs.getTimestamp("uploaded_at")),
                toInstant(rs.getTimestamp("indexed_at"))
        );
    }

    private Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
