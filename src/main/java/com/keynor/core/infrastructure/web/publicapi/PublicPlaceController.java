package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.place.PlaceResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.place.FindAllPlacesUseCase;
import com.keynor.core.domain.port.in.place.FindPlaceByIdUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/places")
public class PublicPlaceController {

    private final FindAllPlacesUseCase findAllPlacesUseCase;
    private final FindPlaceByIdUseCase findPlaceByIdUseCase;

    public PublicPlaceController(
            FindAllPlacesUseCase findAllPlacesUseCase,
            FindPlaceByIdUseCase findPlaceByIdUseCase) {
        this.findAllPlacesUseCase = findAllPlacesUseCase;
        this.findPlaceByIdUseCase = findPlaceByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<PlaceResponse>> findAll(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of(),
                tags != null ? tags : List.of());
        var result = findAllPlacesUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, PlaceResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(PlaceResponse.from(findPlaceByIdUseCase.findById(id)));
    }
}
