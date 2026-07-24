package com.keynor.core.domain.model.shared;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * The stateless unlock session -- the set of hidden entities a visitor has
 * already solved the riddle for, or `all` if the master password was used.
 * Signed and carried by the client, not stored server-side. See root
 * ARCHITECTURE.md -- "Cross-Project Feature: Hidden Content & Black Pins".
 */
public record HiddenUnlockToken(Set<String> unlockedKeys, boolean all, Instant expiresAt) {

    public static String key(EntityType type, UUID id) {
        return type.name() + ":" + id;
    }

    public boolean grantsAccess(EntityType type, UUID id) {
        return all || unlockedKeys.contains(key(type, id));
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
