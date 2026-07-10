package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.event.EventResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.event.FindAllEventsUseCase;
import com.keynor.core.domain.port.in.event.FindEventByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/events")
public class PublicEventController {

    private final FindAllEventsUseCase findAllEventsUseCase;
    private final FindEventByIdUseCase findEventByIdUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public PublicEventController(
            FindAllEventsUseCase findAllEventsUseCase,
            FindEventByIdUseCase findEventByIdUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.findAllEventsUseCase = findAllEventsUseCase;
        this.findEventByIdUseCase = findEventByIdUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventResponse>> findAll(
            @RequestParam String language,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                LanguageRequestParser.parse(language),
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of());
        var result = findAllEventsUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                event -> EventResponse.from(event, findLinkedEntitiesUseCase.findLinks(EntityType.EVENT, event.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> findById(@PathVariable UUID id) {
        var event = findEventByIdUseCase.findById(id);
        return ResponseEntity.ok(EventResponse.from(event, findLinkedEntitiesUseCase.findLinks(EntityType.EVENT, event.getId())));
    }
}
