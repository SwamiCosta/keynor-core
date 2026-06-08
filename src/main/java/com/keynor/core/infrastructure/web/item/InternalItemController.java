package com.keynor.core.infrastructure.web.item;

import com.keynor.core.application.dto.item.CreateItemRequest;
import com.keynor.core.application.dto.item.ItemResponse;
import com.keynor.core.application.dto.item.UpdateItemRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.item.*;
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

    public InternalItemController(
            CreateItemUseCase createItemUseCase,
            UpdateItemUseCase updateItemUseCase,
            ChangeItemStatusUseCase changeItemStatusUseCase,
            DeleteItemUseCase deleteItemUseCase,
            FindItemByIdUseCase findItemByIdUseCase,
            FindAllItemsUseCase findAllItemsUseCase) {
        this.createItemUseCase = createItemUseCase;
        this.updateItemUseCase = updateItemUseCase;
        this.changeItemStatusUseCase = changeItemStatusUseCase;
        this.deleteItemUseCase = deleteItemUseCase;
        this.findItemByIdUseCase = findItemByIdUseCase;
        this.findAllItemsUseCase = findAllItemsUseCase;
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
        return ResponseEntity.ok(PagedResponse.from(result, ItemResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ItemResponse.from(findItemByIdUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
        List<ItemCategory> categories = request.categories().stream()
                .map(c -> ItemCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new CreateItemUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline);
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.from(createItemUseCase.create(command)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateItemRequest request) {
        List<ItemCategory> categories = request.categories().stream()
                .map(c -> ItemCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new UpdateItemUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(),
                request.images() != null ? request.images() : List.of(),
                categories, timeline);
        return ResponseEntity.ok(ItemResponse.from(updateItemUseCase.update(id, command)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ItemResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(ItemResponse.from(
                changeItemStatusUseCase.changeStatus(id, EntityStatus.valueOf(request.status().toUpperCase()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteItemUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
