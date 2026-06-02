package com.keynor.core.domain.exception;

import com.keynor.core.domain.model.shared.EntityStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(EntityStatus from, EntityStatus to) {
        super("Invalid status transition: " + from + " -> " + to);
    }
}
