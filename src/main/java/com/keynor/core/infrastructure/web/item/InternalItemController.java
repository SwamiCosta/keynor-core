package com.keynor.core.infrastructure.web.item;

import com.keynor.core.application.dto.item.CreateItemRequest;
import com.keynor.core.application.dto.item.ItemResponse;
import com.keynor.core.application.dto.item.UpdateItemRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.item.*;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
public class InternalItemController {

    private final CreateItemUseCase createItemUseCase;
    private final UpdateItemUseCase updateItemUseCase;
    private final ChangeItemStatusUseCase changeItemStatusUseCase;
    private final DeleteItemUseCase deleteItemUseCase;
    private final FindItemByIdUseCase findItemByIdUseCase;
    private final FindAllItemsUseCase findAllItemsUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public InternalItemController(
            CreateItemUseCase createItemUseCase,
            UpdateItemUseCase updateItemUseCase,
            ChangeItemStatusUseCase changeItemStatusUseCase,
            DeleteItemUseCase deleteItemUseCase,
            FindItemByIdUseCase findItemByIdUseCase,
            FindAllItemsUseCase findAllItemsUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.createItemUseCase = createItemUseCase;
        this.updateItemUseCase = updateItemUseCase;
        this.changeItemStatusUseCase = changeItemStatusUseCase;
        this.deleteItemUseCase = deleteItemUseCase;
        this.findItemByIdUseCase = findItemByIdUseCase;
        this.findAllItemsUseCase = findAllItemsUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ItemResponse>> findAll(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList() : List.of();
        EntityFilter filter = new EntityFilter(parsedStatuses, categories != null ? categories : List.of(), tags != null ? tags : List.of());
        var result = findAllItemsUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                item -> ItemResponse.from(item, findLinkedEntitiesUseCase.findLinks(EntityType.ITEM, item.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> findById(@PathVariable UUID id) {
        var item = findItemByIdUseCase.findById(id);
        return ResponseEntity.ok(ItemResponse.from(item, findLinkedEntitiesUseCase.findLinks(EntityType.ITEM, item.getId())));
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
        List<ItemCategory> categories = request.categories().stream()
                .map(c -> ItemCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new CreateItemUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline, links);
        var created = createItemUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.from(created, findLinkedEntitiesUseCase.findLinks(EntityType.ITEM, created.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateItemRequest request) {
        List<ItemCategory> categories = request.categories().stream()
                .map(c -> ItemCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new UpdateItemUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline, links);
        var updated = updateItemUseCase.update(id, command);
        return ResponseEntity.ok(ItemResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.ITEM, updated.getId())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ItemResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        var updated = changeItemStatusUseCase.changeStatus(id, EntityStatus.valueOf(request.status().toUpperCase()));
        return ResponseEntity.ok(ItemResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.ITEM, updated.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteItemUseCase.delete(id);
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
