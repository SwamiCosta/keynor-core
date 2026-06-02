package com.keynor.core.infrastructure.web.event;

import com.keynor.core.application.dto.event.CreateEventRequest;
import com.keynor.core.application.dto.event.EventResponse;
import com.keynor.core.application.dto.event.UpdateEventRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.event.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final ChangeEventStatusUseCase changeEventStatusUseCase;
    private final DeleteEventUseCase deleteEventUseCase;
    private final FindEventByIdUseCase findEventByIdUseCase;
    private final FindAllEventsUseCase findAllEventsUseCase;

    public EventController(
            CreateEventUseCase createEventUseCase,
            UpdateEventUseCase updateEventUseCase,
            ChangeEventStatusUseCase changeEventStatusUseCase,
            DeleteEventUseCase deleteEventUseCase,
            FindEventByIdUseCase findEventByIdUseCase,
            FindAllEventsUseCase findAllEventsUseCase) {
        this.createEventUseCase = createEventUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.changeEventStatusUseCase = changeEventStatusUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
        this.findEventByIdUseCase = findEventByIdUseCase;
        this.findAllEventsUseCase = findAllEventsUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventResponse>> findAll(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList() : List.of();
        EntityFilter filter = new EntityFilter(parsedStatuses, categories != null ? categories : List.of(), tags != null ? tags : List.of());
        var result = findAllEventsUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, EventResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(EventResponse.from(findEventByIdUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        List<EventCategory> categories = request.categories().stream()
                .map(c -> EventCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new CreateEventUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(), categories, timeline);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(createEventUseCase.create(command)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
        List<EventCategory> categories = request.categories().stream()
                .map(c -> EventCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new UpdateEventUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(), categories, timeline);
        return ResponseEntity.ok(EventResponse.from(updateEventUseCase.update(id, command)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(EventResponse.from(
                changeEventStatusUseCase.changeStatus(id, EntityStatus.valueOf(request.status().toUpperCase()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteEventUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
