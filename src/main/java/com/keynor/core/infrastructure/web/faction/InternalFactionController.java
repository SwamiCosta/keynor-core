package com.keynor.core.infrastructure.web.faction;

import com.keynor.core.application.dto.faction.CreateFactionRequest;
import com.keynor.core.application.dto.faction.FactionResponse;
import com.keynor.core.application.dto.faction.UpdateFactionRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.faction.*;
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

    public InternalFactionController(
            CreateFactionUseCase createFactionUseCase,
            UpdateFactionUseCase updateFactionUseCase,
            ChangeFactionStatusUseCase changeFactionStatusUseCase,
            DeleteFactionUseCase deleteFactionUseCase,
            FindFactionByIdUseCase findFactionByIdUseCase,
            FindAllFactionsUseCase findAllFactionsUseCase) {
        this.createFactionUseCase = createFactionUseCase;
        this.updateFactionUseCase = updateFactionUseCase;
        this.changeFactionStatusUseCase = changeFactionStatusUseCase;
        this.deleteFactionUseCase = deleteFactionUseCase;
        this.findFactionByIdUseCase = findFactionByIdUseCase;
        this.findAllFactionsUseCase = findAllFactionsUseCase;
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
        return ResponseEntity.ok(PagedResponse.from(result, FactionResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(FactionResponse.from(findFactionByIdUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<FactionResponse> create(@Valid @RequestBody CreateFactionRequest request) {
        List<FactionCategory> categories = request.categories().stream()
                .map(c -> FactionCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new CreateFactionUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(), categories, timeline);
        return ResponseEntity.status(HttpStatus.CREATED).body(FactionResponse.from(createFactionUseCase.create(command)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FactionResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateFactionRequest request) {
        List<FactionCategory> categories = request.categories().stream()
                .map(c -> FactionCategory.valueOf(c.toUpperCase())).toList();
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new UpdateFactionUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(), categories, timeline);
        return ResponseEntity.ok(FactionResponse.from(updateFactionUseCase.update(id, command)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FactionResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(FactionResponse.from(
                changeFactionStatusUseCase.changeStatus(id, EntityStatus.valueOf(request.status().toUpperCase()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteFactionUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
