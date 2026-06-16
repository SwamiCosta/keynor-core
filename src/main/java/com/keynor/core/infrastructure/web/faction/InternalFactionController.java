package com.keynor.core.infrastructure.web.faction;

import com.keynor.core.application.dto.faction.CreateFactionRequest;
import com.keynor.core.application.dto.faction.FactionResponse;
import com.keynor.core.application.dto.faction.UpdateFactionRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.faction.*;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/factions")
public class InternalFactionController {

    private final CreateFactionUseCase createFactionUseCase;
    private final UpdateFactionUseCase updateFactionUseCase;
    private final ChangeFactionStatusUseCase changeFactionStatusUseCase;
    private final DeleteFactionUseCase deleteFactionUseCase;
    private final FindFactionByIdUseCase findFactionByIdUseCase;
    private final FindAllFactionsUseCase findAllFactionsUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public InternalFactionController(
            CreateFactionUseCase createFactionUseCase,
            UpdateFactionUseCase updateFactionUseCase,
            ChangeFactionStatusUseCase changeFactionStatusUseCase,
            DeleteFactionUseCase deleteFactionUseCase,
            FindFactionByIdUseCase findFactionByIdUseCase,
            FindAllFactionsUseCase findAllFactionsUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.createFactionUseCase = createFactionUseCase;
        this.updateFactionUseCase = updateFactionUseCase;
        this.changeFactionStatusUseCase = changeFactionStatusUseCase;
        this.deleteFactionUseCase = deleteFactionUseCase;
        this.findFactionByIdUseCase = findFactionByIdUseCase;
        this.findAllFactionsUseCase = findAllFactionsUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<FactionResponse>> findAll(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList() : List.of();
        EntityFilter filter = new EntityFilter(parsedStatuses, categories != null ? categories : List.of(), tags != null ? tags : List.of());
        var result = findAllFactionsUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                faction -> FactionResponse.from(faction, findLinkedEntitiesUseCase.findLinks(EntityType.FACTION, faction.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactionResponse> findById(@PathVariable UUID id) {
        var faction = findFactionByIdUseCase.findById(id);
        return ResponseEntity.ok(FactionResponse.from(faction, findLinkedEntitiesUseCase.findLinks(EntityType.FACTION, faction.getId())));
    }

    @PostMapping
    public ResponseEntity<FactionResponse> create(@Valid @RequestBody CreateFactionRequest request) {
        List<FactionCategory> categories = request.categories().stream()
                .map(c -> FactionCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new CreateFactionUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline, links);
        var created = createFactionUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(FactionResponse.from(created, findLinkedEntitiesUseCase.findLinks(EntityType.FACTION, created.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FactionResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateFactionRequest request) {
        List<FactionCategory> categories = request.categories().stream()
                .map(c -> FactionCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new UpdateFactionUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline, links);
        var updated = updateFactionUseCase.update(id, command);
        return ResponseEntity.ok(FactionResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.FACTION, updated.getId())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FactionResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        var updated = changeFactionStatusUseCase.changeStatus(id, EntityStatus.valueOf(request.status().toUpperCase()));
        return ResponseEntity.ok(FactionResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.FACTION, updated.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteFactionUseCase.delete(id);
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
