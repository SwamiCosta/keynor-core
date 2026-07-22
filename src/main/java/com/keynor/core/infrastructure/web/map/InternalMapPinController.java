package com.keynor.core.infrastructure.web.map;

import com.keynor.core.application.dto.map.CreateMapPinRequest;
import com.keynor.core.application.dto.map.MapPinResponse;
import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.in.map.CreateMapPinUseCase;
import com.keynor.core.domain.port.in.map.DeleteMapPinUseCase;
import com.keynor.core.domain.port.in.shared.FindEntitySummaryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maps/{mapId}/pins")
public class InternalMapPinController {

    private final CreateMapPinUseCase createMapPinUseCase;
    private final DeleteMapPinUseCase deleteMapPinUseCase;
    private final FindEntitySummaryUseCase findEntitySummaryUseCase;

    public InternalMapPinController(
            CreateMapPinUseCase createMapPinUseCase,
            DeleteMapPinUseCase deleteMapPinUseCase,
            FindEntitySummaryUseCase findEntitySummaryUseCase) {
        this.createMapPinUseCase = createMapPinUseCase;
        this.deleteMapPinUseCase = deleteMapPinUseCase;
        this.findEntitySummaryUseCase = findEntitySummaryUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MapPinResponse> create(@PathVariable String mapId, @Valid @RequestBody CreateMapPinRequest request) {
        var command = new CreateMapPinUseCase.Command(
                mapId,
                EntityType.valueOf(request.entityType().toUpperCase()),
                request.entityId(),
                request.normalizedX(),
                request.normalizedY());
        MapPin created = createMapPinUseCase.create(command);
        var summary = findEntitySummaryUseCase.findSummary(created.getEntityType(), created.getEntityId())
                .orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(MapPinResponse.from(created, summary));
    }

    @DeleteMapping("/{pinId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String mapId, @PathVariable UUID pinId) {
        deleteMapPinUseCase.delete(mapId, pinId);
        return ResponseEntity.noContent().build();
    }
}
