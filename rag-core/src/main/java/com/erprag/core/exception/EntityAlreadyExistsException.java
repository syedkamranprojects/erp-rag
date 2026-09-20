package com.erprag.core.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String entityCode) {
        super("An entity with code '" + entityCode + "' already exists");
    }
}
