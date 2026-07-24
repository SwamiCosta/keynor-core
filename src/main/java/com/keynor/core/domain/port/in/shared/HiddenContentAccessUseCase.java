package com.keynor.core.domain.port.in.shared;

import com.keynor.core.domain.model.shared.EntityType;

import java.time.Instant;
import java.util.UUID;

public interface HiddenContentAccessUseCase {

    record UnlockResult(String token, boolean unlockedAll, Instant expiresAt) {}

    UnlockResult unlock(EntityType type, UUID id, String password, String existingToken);

    boolean hasAccess(String token, EntityType type, UUID id);

    String findRiddle(EntityType type, UUID id);
}
