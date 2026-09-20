CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE knowledge_base (
    id BIGSERIAL PRIMARY KEY,
    entity_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE knowledge_base_user (
    id BIGSERIAL PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL REFERENCES knowledge_base(id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (knowledge_base_id, username)
);

CREATE TABLE document (
    id UUID PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL REFERENCES knowledge_base(id) ON DELETE CASCADE,
    original_filename VARCHAR(512) NOT NULL,
    content_type VARCHAR(128),
    storage_path VARCHAR(1024) NOT NULL,
    status VARCHAR(16) NOT NULL,
    chunk_count INT,
    error_message TEXT,
    uploaded_by VARCHAR(100),
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    indexed_at TIMESTAMPTZ
);

CREATE INDEX idx_document_knowledge_base ON document(knowledge_base_id);
