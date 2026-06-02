package com.keynor.core.domain.port.in.event;

import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.shared.EntityStatus;

import java.util.UUID;

public interface ChangeEventStatusUseCase {
    Event changeStatus(UUID id, EntityStatus newStatus);
}
