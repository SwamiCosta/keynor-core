package com.keynor.core.domain.model.shared;

import java.time.Instant;
import java.util.UUID;

/**
 * The riddle/password lock for a single hidden entity. One lock per
 * (entityType, entityId) -- see root ARCHITECTURE.md, "Cross-Project
 * Feature: Hidden Content & Black Pins". passwordHash is a BCrypt hash,
 * never the raw password.
 */
public record HiddenContentLock(
        EntityType entityType,
        UUID entityId,
        String riddleText,
        String passwordHash,
        Instant createdAt,
        Instant updatedAt) {
}
