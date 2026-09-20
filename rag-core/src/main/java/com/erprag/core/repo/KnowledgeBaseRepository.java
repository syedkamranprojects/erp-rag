package com.erprag.core.repo;

import com.erprag.core.domain.KnowledgeBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class KnowledgeBaseRepository {

    private final DataSource dataSource;

    public KnowledgeBaseRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public KnowledgeBase insert(String entityCode, String name) {
        String sql = """
                INSERT INTO knowledge_base (entity_code, name)
                VALUES (?, ?)
                RETURNING id, entity_code, name, created_at, updated_at
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entityCode);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert knowledge base", e);
        }
    }

    public Optional<KnowledgeBase> findByEntityCode(String entityCode) {
        String sql = "SELECT id, entity_code, name, created_at, updated_at FROM knowledge_base WHERE entity_code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entityCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up knowledge base", e);
        }
    }

    public List<KnowledgeBase> findAll() {
        String sql = "SELECT id, entity_code, name, created_at, updated_at FROM knowledge_base ORDER BY created_at";
        List<KnowledgeBase> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list knowledge bases", e);
        }
    }

    public Optional<KnowledgeBase> updateName(String entityCode, String newName) {
        String sql = """
                UPDATE knowledge_base SET name = ?, updated_at = now()
                WHERE entity_code = ?
                RETURNING id, entity_code, name, created_at, updated_at
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, entityCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update knowledge base", e);
        }
    }

    public boolean deleteByEntityCode(String entityCode) {
        String sql = "DELETE FROM knowledge_base WHERE entity_code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entityCode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete knowledge base", e);
        }
    }

    private KnowledgeBase map(ResultSet rs) throws SQLException {
        return new KnowledgeBase(
                rs.getLong("id"),
                rs.getString("entity_code"),
                rs.getString("name"),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at"))
        );
    }

    private Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
