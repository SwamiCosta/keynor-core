package com.keynor.core.infrastructure.web.place;

import com.keynor.core.application.dto.place.CreatePlaceRequest;
import com.keynor.core.application.dto.place.PlaceResponse;
import com.keynor.core.application.dto.place.UpdatePlaceRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.model.place.PlaceCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.place.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/places")
public class InternalPlaceController {

    private final CreatePlaceUseCase createPlaceUseCase;
    private final UpdatePlaceUseCase updatePlaceUseCase;
    private final ChangePlaceStatusUseCase changePlaceStatusUseCase;
    private final DeletePlaceUseCase deletePlaceUseCase;
    private final FindPlaceByIdUseCase findPlaceByIdUseCase;
    private final FindAllPlacesUseCase findAllPlacesUseCase;

    public InternalPlaceController(
            CreatePlaceUseCase createPlaceUseCase,
            UpdatePlaceUseCase updatePlaceUseCase,
            ChangePlaceStatusUseCase changePlaceStatusUseCase,
            DeletePlaceUseCase deletePlaceUseCase,
            FindPlaceByIdUseCase findPlaceByIdUseCase,
            FindAllPlacesUseCase findAllPlacesUseCase) {
        this.createPlaceUseCase = createPlaceUseCase;
        this.updatePlaceUseCase = updatePlaceUseCase;
        this.changePlaceStatusUseCase = changePlaceStatusUseCase;
        this.deletePlaceUseCase = deletePlaceUseCase;
        this.findPlaceByIdUseCase = findPlaceByIdUseCase;
        this.findAllPlacesUseCase = findAllPlacesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<PlaceResponse>> findAll(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList()
                : List.of();
        EntityFilter filter = new EntityFilter(parsedStatuses, categories != null ? categories : List.of(), tags != null ? tags : List.of());
        var result = findAllPlacesUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, PlaceResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(PlaceResponse.from(findPlaceByIdUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> create(@Valid @RequestBody CreatePlaceRequest request) {
        List<PlaceCategory> categories = request.categories().stream()
                .map(c -> PlaceCategory.valueOf(c.toUpperCase())).toList();
        MapType mapType = request.mapType() != null ? MapType.valueOf(request.mapType().toUpperCase()) : null;
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new CreatePlaceUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(),
                categories, mapType, timeline);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlaceResponse.from(createPlaceUseCase.create(command)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdatePlaceRequest request) {
        List<PlaceCategory> categories = request.categories().stream()
                .map(c -> PlaceCategory.valueOf(c.toUpperCase())).toList();
        MapType mapType = request.mapType() != null ? MapType.valueOf(request.mapType().toUpperCase()) : null;
        Timeline timeline = (request.timelineFoundedEra() != null || request.timelineDestroyedEra() != null)
                ? new Timeline(request.timelineFoundedEra(), request.timelineDestroyedEra()) : null;
        var command = new UpdatePlaceUseCase.Command(
                request.name(), request.summary(), request.body(),
                request.tags() != null ? request.tags() : List.of(),
                categories, mapType, timeline);
        return ResponseEntity.ok(PlaceResponse.from(updatePlaceUseCase.update(id, command)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PlaceResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        EntityStatus newStatus = EntityStatus.valueOf(request.status().toUpperCase());
        return ResponseEntity.ok(PlaceResponse.from(changePlaceStatusUseCase.changeStatus(id, newStatus)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePlaceUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
