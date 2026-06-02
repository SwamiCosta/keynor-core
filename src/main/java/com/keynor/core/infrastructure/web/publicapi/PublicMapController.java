package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.map.MapResponse;
import com.keynor.core.domain.port.in.map.FindAllMapsUseCase;
import com.keynor.core.domain.port.in.map.FindMapByIdUseCase;
import com.keynor.core.domain.port.in.map.FindMapsByEraUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/v1/maps")
public class PublicMapController {

    private final FindAllMapsUseCase findAllMapsUseCase;
    private final FindMapByIdUseCase findMapByIdUseCase;
    private final FindMapsByEraUseCase findMapsByEraUseCase;

    public PublicMapController(
            FindAllMapsUseCase findAllMapsUseCase,
            FindMapByIdUseCase findMapByIdUseCase,
            FindMapsByEraUseCase findMapsByEraUseCase) {
        this.findAllMapsUseCase = findAllMapsUseCase;
        this.findMapByIdUseCase = findMapByIdUseCase;
        this.findMapsByEraUseCase = findMapsByEraUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MapResponse>> findAll(@RequestParam(required = false) String eraId) {
        List<MapResponse> maps = eraId != null
                ? findMapsByEraUseCase.findByEraId(eraId).stream().map(MapResponse::from).toList()
                : findAllMapsUseCase.findAll().stream().map(MapResponse::from).toList();
        return ResponseEntity.ok(maps);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MapResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(MapResponse.from(findMapByIdUseCase.findById(id)));
    }
}
