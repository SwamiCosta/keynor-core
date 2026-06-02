package com.keynor.core.infrastructure.web.lore;

import com.keynor.core.application.dto.lore.CreateLoreRequest;
import com.keynor.core.application.dto.lore.LoreResponse;
import com.keynor.core.application.dto.lore.UpdateLoreRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.lore.LoreCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.lore.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lore")
public class LoreController {

    private final CreateLoreUseCase createLoreUseCase;
    private final UpdateLoreUseCase updateLoreUseCase;
    private final ChangeLoreStatusUseCase changeLoreStatusUseCase;
    private final DeleteLoreUseCase deleteLoreUseCase;
    private final FindLoreByIdUseCase findLoreByIdUseCase;
    private final FindAllLoreUseCase findAllLoreUseCase;

    public LoreController(
            CreateLoreUseCase createLoreUseCase,
            UpdateLoreUseCase updateLoreUseCase,
            ChangeLoreStatusUseCase changeLoreStatusUseCase,
            DeleteLoreUseCase deleteLoreUseCase,
            FindLoreByIdUseCase findLoreByIdUseCase,
            FindAllLoreUseCase findAllLoreUseCase) {
        this.createLoreUseCase = createLoreUseCase;
        this.updateLoreUseCase = updateLoreUseCase;
        this.changeLoreStatusUseCase = changeLoreStatusUseCase;
        this.deleteLoreUseCase = deleteLoreUseCase;
        this.findLoreByIdUseCase = findLoreByIdUseCase;
        this.findAllLoreUseCase = findAllLoreUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LoreResponse>> findAll(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList() : List.of();
        EntityFilter filter = new EntityFilter(parsedStatuses, categories != null ? categories : List.of(), tags != null ? tags : List.of());
        var result = findAllLoreUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, LoreResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoreResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(LoreResponse.from(findLoreByIdUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<LoreResponse> create(@Valid @RequestBody CreateLoreRequest request) {
        List<LoreCategory> categories = request.categories().stream()
                .map(c -> LoreCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new CreateLoreUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(), categories, timeline);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoreResponse.from(createLoreUseCase.create(command)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoreResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateLoreRequest request) {
        List<LoreCategory> categories = request.categories().stream()
                .map(c -> LoreCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new UpdateLoreUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(), categories, timeline);
        return ResponseEntity.ok(LoreResponse.from(updateLoreUseCase.update(id, command)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LoreResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(LoreResponse.from(
                changeLoreStatusUseCase.changeStatus(id, EntityStatus.valueOf(request.status().toUpperCase()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteLoreUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
