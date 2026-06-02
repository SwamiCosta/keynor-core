package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Event save(Event event);
    Optional<Event> findById(UUID id);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    void deleteById(UUID id);
    PageResult<Event> findAll(EntityFilter filter, PageRequest pageRequest);
}
