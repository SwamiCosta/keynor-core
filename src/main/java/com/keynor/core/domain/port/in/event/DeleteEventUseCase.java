package com.keynor.core.domain.port.in.event;

import java.util.UUID;

public interface DeleteEventUseCase {
    void delete(UUID id);
}
