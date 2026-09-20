package com.erprag.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class FileStorage {

    private final Path basePath;

    public FileStorage(String basePath) {
        this.basePath = Path.of(basePath);
    }

    public Path save(String entityCode, UUID documentId, String filename, InputStream content) {
        try {
            Path dir = basePath.resolve(entityCode);
            Files.createDirectories(dir);
            Path target = dir.resolve(documentId + "-" + filename);
            Files.copy(content, target);
            return target;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file '" + filename + "'", e);
        }
    }

    public InputStream open(Path storagePath) {
        try {
            return Files.newInputStream(storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open stored file '" + storagePath + "'", e);
        }
    }
}
