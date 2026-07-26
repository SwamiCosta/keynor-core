package com.keynor.core.domain.port.in.shared;

import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.HiddenContentLock;

import java.util.UUID;

public interface CreateHiddenContentLockUseCase {

    HiddenContentLock createOrReplace(EntityType type, UUID id, String riddleText, String rawPassword);
}
