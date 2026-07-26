package com.keynor.core.domain.exception;

import com.keynor.core.domain.model.shared.EntityType;

import java.util.UUID;

public class HiddenContentLinkViolationException extends RuntimeException {

    public HiddenContentLinkViolationException(EntityType targetType, UUID targetId) {
        super("A visible entity may not link to hidden content: " + targetType + " " + targetId);
    }
}
