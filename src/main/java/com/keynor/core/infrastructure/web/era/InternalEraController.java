package com.keynor.core.infrastructure.web.era;

import com.keynor.core.application.dto.era.CreateEraRequest;
import com.keynor.core.application.dto.era.EraResponse;
import com.keynor.core.domain.model.era.EraImportance;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.port.in.era.CreateEraUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/eras")
public class InternalEraController {

    private final CreateEraUseCase createEraUseCase;

    public InternalEraController(CreateEraUseCase createEraUseCase) {
        this.createEraUseCase = createEraUseCase;
    }

    @PostMapping
    public ResponseEntity<EraResponse> create(@Valid @RequestBody CreateEraRequest request) {
        EraType type = EraType.valueOf(request.type().toUpperCase());
        EraImportance importance = request.importance() != null
                ? EraImportance.valueOf(request.importance().toUpperCase())
                : null;
        var command = new CreateEraUseCase.Command(
                request.name(),
                request.orderIndex(),
                type,
                importance,
                request.description());
        var created = createEraUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(EraResponse.from(created));
    }
}
