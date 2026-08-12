package com.keynor.core.infrastructure.web.event;

import com.keynor.core.application.dto.event.CreateEventRequest;
import com.keynor.core.application.dto.event.EventResponse;
import com.keynor.core.application.dto.event.UpdateEventRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.event.*;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import com.keynor.core.infrastructure.web.shared.EntityStatusRequestParser;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class InternalEventController {

    private final CreateEventUseCase createEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final ChangeEventStatusUseCase changeEventStatusUseCase;
    private final DeleteEventUseCase deleteEventUseCase;
    private final FindEventByIdUseCase findEventByIdUseCase;
    private final FindAllEventsUseCase findAllEventsUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public InternalEventController(
            CreateEventUseCase createEventUseCase,
            UpdateEventUseCase updateEventUseCase,
            ChangeEventStatusUseCase changeEventStatusUseCase,
            DeleteEventUseCase deleteEventUseCase,
            FindEventByIdUseCase findEventByIdUseCase,
            FindAllEventsUseCase findAllEventsUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.createEventUseCase = createEventUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.changeEventStatusUseCase = changeEventStatusUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
        this.findEventByIdUseCase = findEventByIdUseCase;
        this.findAllEventsUseCase = findAllEventsUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventResponse>> findAll(
            @RequestParam String language,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList() : List.of();
        EntityFilter filter = new EntityFilter(LanguageRequestParser.parse(language), parsedStatuses, categories != null ? categories : List.of(), false, false);
        var result = findAllEventsUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                event -> EventResponse.from(event, findLinkedEntitiesUseCase.findLinks(EntityType.EVENT, event.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> findById(@PathVariable UUID id) {
        var event = findEventByIdUseCase.findById(id);
        return ResponseEntity.ok(EventResponse.from(event, findLinkedEntitiesUseCase.findLinks(EntityType.EVENT, event.getId())));
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        List<EventCategory> categories = request.categories().stream()
                .map(c -> EventCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        List<EntityLinkRef> links = toLinkRefs(request.links());
        EntityStatus initialStatus = EntityStatusRequestParser.parseCreationStatus(request.status());
        var command = new CreateEventUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline, initialStatus,
                LanguageRequestParser.parse(request.language()),
                request.translationGroupId(),
                request.versionGroupId(),
                links,
                request.hidden(),
                request.riddleText(),
                request.password(),
                request.common());
        var created = createEventUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(created, findLinkedEntitiesUseCase.findLinks(EntityType.EVENT, created.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
        List<EventCategory> categories = request.categories().stream()
                .map(c -> EventCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new UpdateEventUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline, links,
                request.hidden(), request.riddleText(), request.password(),
                request.common());
        var updated = updateEventUseCase.update(id, command);
        return ResponseEntity.ok(EventResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.EVENT, updated.getId())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        var updated = changeEventStatusUseCase.changeStatus(id, EntityStatus.valueOf(request.status().toUpperCase()));
        return ResponseEntity.ok(EventResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.EVENT, updated.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteEventUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityLinkRef> toLinkRefs(List<com.keynor.core.application.dto.shared.EntityLinkRequest> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream()
                .map(link -> new EntityLinkRef(EntityType.valueOf(link.targetType().toUpperCase()), link.targetId()))
                .toList();
    }
}
