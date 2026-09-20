package com.erprag.core.repo;

import com.erprag.core.domain.KnowledgeBaseUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;

public class KnowledgeBaseUserRepository {

    private final DataSource dataSource;

    public KnowledgeBaseUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public KnowledgeBaseUser insert(long knowledgeBaseId, String username, String passwordHash, String role) {
        String sql = """
                INSERT INTO knowledge_base_user (knowledge_base_id, username, password_hash, role)
                VALUES (?, ?, ?, ?)
                RETURNING id, knowledge_base_id, username, password_hash, role, created_at
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, knowledgeBaseId);
            ps.setString(2, username);
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert knowledge base user", e);
        }
    }

    public Optional<KnowledgeBaseUser> findByKnowledgeBaseAndUsername(long knowledgeBaseId, String username) {
        String sql = """
                SELECT id, knowledge_base_id, username, password_hash, role, created_at
                FROM knowledge_base_user
                WHERE knowledge_base_id = ? AND username = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, knowledgeBaseId);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up knowledge base user", e);
        }
    }

    private KnowledgeBaseUser map(ResultSet rs) throws SQLException {
        return new KnowledgeBaseUser(
                rs.getLong("id"),
                rs.getLong("knowledge_base_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role"),
                toInstant(rs.getTimestamp("created_at"))
        );
    }

    private Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
