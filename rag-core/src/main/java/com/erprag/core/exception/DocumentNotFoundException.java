package com.erprag.core.exception;

import java.util.UUID;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(UUID documentId) {
        super("No document found with id '" + documentId + "'");
    }
}
