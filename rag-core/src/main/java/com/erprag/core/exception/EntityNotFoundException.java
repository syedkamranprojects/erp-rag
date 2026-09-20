package com.erprag.core.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityCode) {
        super("No entity found with code '" + entityCode + "'");
    }
}
