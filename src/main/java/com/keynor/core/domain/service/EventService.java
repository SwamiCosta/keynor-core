package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.event.*;
import com.keynor.core.domain.port.out.EventRepository;

import java.time.Instant;
import java.util.UUID;

public class EventService implements
        CreateEventUseCase,
        UpdateEventUseCase,
        ChangeEventStatusUseCase,
        DeleteEventUseCase,
        FindEventByIdUseCase,
        FindAllEventsUseCase {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event create(CreateEventUseCase.Command command) {
        if (eventRepository.existsByName(command.name())) {
            throw new DuplicateEntityNameException("Event", command.name());
        }
        Instant now = Instant.now();
        Event event = new Event(
                UUID.randomUUID(),
                command.name(),
                command.summary(),
                command.body(),
                command.tags(),
                command.categories(),
                EntityStatus.DRAFT,
                command.timeline(),
                now,
                now);
        return eventRepository.save(event);
    }

    @Override
    public Event update(UUID id, UpdateEventUseCase.Command command) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event", id));
        event.update(command.name(), command.summary(), command.body(), command.tags(), command.categories(), command.timeline());
        return eventRepository.save(event);
    }

    @Override
    public Event changeStatus(UUID id, EntityStatus newStatus) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event", id));
        event.changeStatus(newStatus);
        return eventRepository.save(event);
    }

    @Override
    public void delete(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    public Event findById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event", id));
    }

    @Override
    public PageResult<Event> findAll(EntityFilter filter, PageRequest pageRequest) {
        return eventRepository.findAll(filter, pageRequest);
    }
}
