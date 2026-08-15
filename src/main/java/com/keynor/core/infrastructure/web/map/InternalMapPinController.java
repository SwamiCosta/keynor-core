package com.keynor.core.infrastructure.web.map;

import com.keynor.core.application.dto.map.CreateMapPinRequest;
import com.keynor.core.application.dto.map.MapPinResponse;
import com.keynor.core.application.dto.map.UpdateMapPinRequest;
import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.port.in.map.CreateMapPinUseCase;
import com.keynor.core.domain.port.in.map.DeleteMapPinUseCase;
import com.keynor.core.domain.port.in.map.UpdateMapPinUseCase;
import com.keynor.core.domain.port.in.shared.FindEntitySummaryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.keynor.core.infrastructure.web.map.MapPinRequestSupport.parseEntityType;

@RestController
@RequestMapping("/api/v1/maps/{mapId}/pins")
public class InternalMapPinController {

    private final CreateMapPinUseCase createMapPinUseCase;
    private final UpdateMapPinUseCase updateMapPinUseCase;
    private final DeleteMapPinUseCase deleteMapPinUseCase;
    private final FindEntitySummaryUseCase findEntitySummaryUseCase;

    public InternalMapPinController(
            CreateMapPinUseCase createMapPinUseCase,
            UpdateMapPinUseCase updateMapPinUseCase,
            DeleteMapPinUseCase deleteMapPinUseCase,
            FindEntitySummaryUseCase findEntitySummaryUseCase) {
        this.createMapPinUseCase = createMapPinUseCase;
        this.updateMapPinUseCase = updateMapPinUseCase;
        this.deleteMapPinUseCase = deleteMapPinUseCase;
        this.findEntitySummaryUseCase = findEntitySummaryUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MapPinResponse> create(@PathVariable String mapId, @Valid @RequestBody CreateMapPinRequest request) {
        var command = new CreateMapPinUseCase.Command(
                mapId,
                parseEntityType(request.entityType()),
                request.entityId(),
                request.name(),
                request.normalizedX(),
                request.normalizedY());
        MapPin created = createMapPinUseCase.create(command);
        var summary = created.getEntityType() == null
                ? null
                : findEntitySummaryUseCase.findSummary(created.getEntityType(), created.getEntityId()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(MapPinResponse.from(created, summary));
    }

    @PatchMapping("/{pinId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MapPinResponse> update(
            @PathVariable String mapId, @PathVariable UUID pinId, @Valid @RequestBody UpdateMapPinRequest request) {
        var command = new UpdateMapPinUseCase.Command(
                request.normalizedX(),
                request.normalizedY(),
                request.name(),
                parseEntityType(request.entityType()),
                request.entityId());
        MapPin updated = updateMapPinUseCase.update(mapId, pinId, command);
        var summary = updated.getEntityType() == null
                ? null
                : findEntitySummaryUseCase.findSummary(updated.getEntityType(), updated.getEntityId()).orElseThrow();
        return ResponseEntity.ok(MapPinResponse.from(updated, summary));
    }

    @DeleteMapping("/{pinId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String mapId, @PathVariable UUID pinId) {
        deleteMapPinUseCase.delete(mapId, pinId);
        return ResponseEntity.noContent().build();
    }
}
