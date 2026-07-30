package com.keynor.core.infrastructure.web.era;

import com.keynor.core.application.dto.era.CreateEraRequest;
import com.keynor.core.application.dto.era.EraResponse;
import com.keynor.core.application.dto.shared.EntityLinkRequest;
import com.keynor.core.domain.model.era.EraImportance;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.in.era.CreateEraUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eras")
public class InternalEraController {

    private final CreateEraUseCase createEraUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public InternalEraController(
            CreateEraUseCase createEraUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.createEraUseCase = createEraUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @PostMapping
    public ResponseEntity<EraResponse> create(@Valid @RequestBody CreateEraRequest request) {
        EraType type = EraType.valueOf(request.type().toUpperCase());
        EraImportance importance = request.importance() != null
                ? EraImportance.valueOf(request.importance().toUpperCase())
                : null;
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new CreateEraUseCase.Command(
                request.name(),
                request.orderIndex(),
                type,
                importance,
                request.description(),
                LanguageRequestParser.parse(request.language()),
                request.translationGroupId(),
                links);
        var created = createEraUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                EraResponse.from(created, findLinkedEntitiesUseCase.findLinks(EntityType.ERA, created.getId())));
    }

    private List<EntityLinkRef> toLinkRefs(List<EntityLinkRequest> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream()
                .map(link -> new EntityLinkRef(EntityType.valueOf(link.targetType().toUpperCase()), link.targetId()))
                .toList();
    }
}
