package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.event.*;
import com.keynor.core.domain.port.in.shared.CreateHiddenContentLockUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.EventRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EventService implements
        CreateEventUseCase,
        UpdateEventUseCase,
        ChangeEventStatusUseCase,
        DeleteEventUseCase,
        FindEventByIdUseCase,
        FindAllEventsUseCase {

    private final EventRepository eventRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final EraRepository eraRepository;
    private final UniverseEntityLookupRepository universeEntityLookupRepository;
    private final CreateHiddenContentLockUseCase createHiddenContentLockUseCase;

    public EventService(
            EventRepository eventRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository,
            CreateHiddenContentLockUseCase createHiddenContentLockUseCase) {
        this.eventRepository = eventRepository;
        this.entityLinkRepository = entityLinkRepository;
        this.eraRepository = eraRepository;
        this.universeEntityLookupRepository = universeEntityLookupRepository;
        this.createHiddenContentLockUseCase = createHiddenContentLockUseCase;
    }

    @Override
    public Event create(CreateEventUseCase.Command command) {
        if (eventRepository.existsByNameAndLanguage(command.name(), command.language())) {
            throw new DuplicateEntityNameException("Event", command.name());
        }
        validateTimeline(command.timeline());
        Instant now = Instant.now();
        EntityStatus initialStatus = command.status() != null ? command.status() : EntityStatus.DRAFT;
        UUID newId = UUID.randomUUID();
        UUID translationGroupId = command.translationGroupId() != null ? command.translationGroupId() : newId;
        UUID versionGroupId = command.versionGroupId() != null ? command.versionGroupId() : newId;
        Event event = new Event(
                newId,
                command.name(),
                command.summary(),
                command.body(),
                command.images(),
                command.categories(),
                initialStatus,
                command.timeline(),
                now,
                now,
                command.language(),
                translationGroupId,
                versionGroupId,
                command.hidden(),
                command.common());
        Event saved = eventRepository.save(event);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        HiddenLinkDirectionValidator.validate(saved.isHidden(), links, universeEntityLookupRepository);
        entityLinkRepository.replaceLinks(EntityType.EVENT, saved.getId(), links);
        if (saved.isHidden()) {
            createHiddenContentLockUseCase.createOrReplace(EntityType.EVENT, saved.getId(), command.riddleText(), command.password());
        }
        return saved;
    }

    @Override
    public Event update(UUID id, UpdateEventUseCase.Command command) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event", id));
        validateTimeline(command.timeline());
        if (command.hidden() && (command.riddleText() == null || command.riddleText().isBlank()
                || command.password() == null || command.password().isBlank())) {
            throw new IllegalArgumentException("riddleText and password are required when hidden is true");
        }
        event.update(command.name(), command.summary(), command.body(), command.images(), command.categories(), command.timeline());
        event.setHidden(command.hidden());
        event.setCommon(command.common());
        Event saved = eventRepository.save(event);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        HiddenLinkDirectionValidator.validate(saved.isHidden(), links, universeEntityLookupRepository);
        entityLinkRepository.replaceLinks(EntityType.EVENT, saved.getId(), links);
        if (saved.isHidden()) {
            createHiddenContentLockUseCase.createOrReplace(EntityType.EVENT, saved.getId(), command.riddleText(), command.password());
        }
        return saved;
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
        entityLinkRepository.deleteAllForEntity(EntityType.EVENT, id);
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

    private void validateTimeline(Timeline timeline) {
        if (timeline == null) return;
        validateEraName(timeline.founded());
        validateEraName(timeline.destroyed());
    }

    private void validateEraName(String eraName) {
        if (eraName == null) return;
        if (eraRepository.findByName(eraName).isEmpty()) {
            throw new UnknownEraNameException(eraName);
        }
    }
}
