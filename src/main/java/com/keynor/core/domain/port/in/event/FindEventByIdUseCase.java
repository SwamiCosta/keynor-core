package com.keynor.core.domain.port.in.event;

import com.keynor.core.domain.model.event.Event;

import java.util.UUID;

public interface FindEventByIdUseCase {
    Event findById(UUID id);
}
