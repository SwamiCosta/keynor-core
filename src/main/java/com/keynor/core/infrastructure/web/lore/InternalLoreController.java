package com.keynor.core.infrastructure.web.lore;

import com.keynor.core.application.dto.lore.CreateLoreRequest;
import com.keynor.core.application.dto.lore.LoreResponse;
import com.keynor.core.application.dto.lore.UpdateLoreRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.lore.LoreCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.lore.*;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lore")
public class InternalLoreController {

    private final CreateLoreUseCase createLoreUseCase;
    private final UpdateLoreUseCase updateLoreUseCase;
    private final ChangeLoreStatusUseCase changeLoreStatusUseCase;
    private final DeleteLoreUseCase deleteLoreUseCase;
    private final FindLoreByIdUseCase findLoreByIdUseCase;
    private final FindAllLoreUseCase findAllLoreUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public InternalLoreController(
            CreateLoreUseCase createLoreUseCase,
            UpdateLoreUseCase updateLoreUseCase,
            ChangeLoreStatusUseCase changeLoreStatusUseCase,
            DeleteLoreUseCase deleteLoreUseCase,
            FindLoreByIdUseCase findLoreByIdUseCase,
            FindAllLoreUseCase findAllLoreUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.createLoreUseCase = createLoreUseCase;
        this.updateLoreUseCase = updateLoreUseCase;
        this.changeLoreStatusUseCase = changeLoreStatusUseCase;
        this.deleteLoreUseCase = deleteLoreUseCase;
        this.findLoreByIdUseCase = findLoreByIdUseCase;
        this.findAllLoreUseCase = findAllLoreUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LoreResponse>> findAll(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList() : List.of();
        EntityFilter filter = new EntityFilter(parsedStatuses, categories != null ? categories : List.of());
        var result = findAllLoreUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                lore -> LoreResponse.from(lore, findLinkedEntitiesUseCase.findLinks(EntityType.LORE, lore.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoreResponse> findById(@PathVariable UUID id) {
        var lore = findLoreByIdUseCase.findById(id);
        return ResponseEntity.ok(LoreResponse.from(lore, findLinkedEntitiesUseCase.findLinks(EntityType.LORE, lore.getId())));
    }

    @PostMapping
    public ResponseEntity<LoreResponse> create(@Valid @RequestBody CreateLoreRequest request) {
        List<LoreCategory> categories = request.categories().stream()
                .map(c -> LoreCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        EntityStatus initialStatus = parseCreationStatus(request.status());
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new CreateLoreUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline, initialStatus, links);
        var created = createLoreUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                LoreResponse.from(created, findLinkedEntitiesUseCase.findLinks(EntityType.LORE, created.getId())));
    }

    private List<EntityLinkRef> toLinkRefs(List<com.keynor.core.application.dto.shared.EntityLinkRequest> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream()
                .map(link -> new EntityLinkRef(EntityType.valueOf(link.targetType().toUpperCase()), link.targetId()))
                .toList();
    }

    private EntityStatus parseCreationStatus(String rawStatus) {
        if (rawStatus == null) {
            return EntityStatus.DRAFT;
        }
        EntityStatus status = EntityStatus.valueOf(rawStatus.toUpperCase());
        if (status == EntityStatus.DEPRECATED) {
            throw new IllegalArgumentException("Status DEPRECATED is not allowed on creation. Allowed values: DRAFT, CANON");
        }
        return status;
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoreResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateLoreRequest request) {
        List<LoreCategory> categories = request.categories().stream()
                .map(c -> LoreCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new UpdateLoreUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline, links);
        var updated = updateLoreUseCase.update(id, command);
        return ResponseEntity.ok(LoreResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.LORE, updated.getId())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LoreResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        var updated = changeLoreStatusUseCase.changeStatus(id, EntityStatus.valueOf(request.status().toUpperCase()));
        return ResponseEntity.ok(LoreResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.LORE, updated.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteLoreUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
