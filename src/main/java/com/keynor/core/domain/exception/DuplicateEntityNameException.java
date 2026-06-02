package com.keynor.core.domain.exception;

public class DuplicateEntityNameException extends RuntimeException {

    public DuplicateEntityNameException(String entityType, String name) {
        super(entityType + " already exists with name: " + name);
    }
}
