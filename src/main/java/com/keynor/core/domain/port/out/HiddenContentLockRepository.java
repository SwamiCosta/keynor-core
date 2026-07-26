package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.HiddenContentLock;

import java.util.Optional;
import java.util.UUID;

public interface HiddenContentLockRepository {

    Optional<HiddenContentLock> findByEntity(EntityType type, UUID id);

    HiddenContentLock save(HiddenContentLock lock);
}
