package com.keynor.core.infrastructure.web.era;

import com.keynor.core.application.dto.era.EraResponse;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.in.era.FindAllErasUseCase;
import com.keynor.core.domain.port.in.era.FindEraByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/eras")
public class PublicEraController {

    private final FindAllErasUseCase findAllErasUseCase;
    private final FindEraByIdUseCase findEraByIdUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public PublicEraController(
            FindAllErasUseCase findAllErasUseCase,
            FindEraByIdUseCase findEraByIdUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.findAllErasUseCase = findAllErasUseCase;
        this.findEraByIdUseCase = findEraByIdUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<EraResponse>> findAll(@RequestParam String language) {
        return ResponseEntity.ok(
                findAllErasUseCase.findAll(LanguageRequestParser.parse(language)).stream()
                        .map(era -> EraResponse.from(era, findLinkedEntitiesUseCase.findLinks(EntityType.ERA, era.getId())))
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EraResponse> findById(@PathVariable UUID id) {
        var era = findEraByIdUseCase.findById(id);
        return ResponseEntity.ok(EraResponse.from(era, findLinkedEntitiesUseCase.findLinks(EntityType.ERA, era.getId())));
    }
}
