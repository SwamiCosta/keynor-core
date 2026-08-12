package com.keynor.core.infrastructure.web.map;

import com.keynor.core.application.dto.map.MapPinResponse;
import com.keynor.core.domain.port.in.map.FindMapPinsUseCase;
import com.keynor.core.domain.port.in.shared.FindEntitySummaryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/v1/maps/{mapId}/pins")
public class PublicMapPinController {

    private final FindMapPinsUseCase findMapPinsUseCase;
    private final FindEntitySummaryUseCase findEntitySummaryUseCase;

    public PublicMapPinController(FindMapPinsUseCase findMapPinsUseCase, FindEntitySummaryUseCase findEntitySummaryUseCase) {
        this.findMapPinsUseCase = findMapPinsUseCase;
        this.findEntitySummaryUseCase = findEntitySummaryUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MapPinResponse>> findByMap(@PathVariable String mapId) {
        // A pin whose target is common=true is dropped entirely, not just
        // downgraded to a black pin like hidden -- common content has no
        // route to discovery via the map at all, only via another entity's
        // resolved links (see common-content-implementation.md).
        List<MapPinResponse> pins = findMapPinsUseCase.findByMapId(mapId).stream()
                .flatMap(pin -> findEntitySummaryUseCase.findSummary(pin.getEntityType(), pin.getEntityId())
                        .filter(summary -> !summary.common())
                        .map(summary -> MapPinResponse.from(pin, summary))
                        .stream())
                .toList();
        return ResponseEntity.ok(pins);
    }
}
