package com.keynor.core.domain.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityType, UUID id) {
        super(entityType + " not found with id: " + id);
    }
}
