package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.faction.FactionResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.faction.FindAllFactionsUseCase;
import com.keynor.core.domain.port.in.faction.FindFactionByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/factions")
public class PublicFactionController {

    private final FindAllFactionsUseCase findAllFactionsUseCase;
    private final FindFactionByIdUseCase findFactionByIdUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public PublicFactionController(
            FindAllFactionsUseCase findAllFactionsUseCase,
            FindFactionByIdUseCase findFactionByIdUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.findAllFactionsUseCase = findAllFactionsUseCase;
        this.findFactionByIdUseCase = findFactionByIdUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<FactionResponse>> findAll(
            @RequestParam String language,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                LanguageRequestParser.parse(language),
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of(),
                true);
        var result = findAllFactionsUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                faction -> FactionResponse.from(faction, findLinkedEntitiesUseCase.findLinks(EntityType.FACTION, faction.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactionResponse> findById(@PathVariable UUID id) {
        var faction = findFactionByIdUseCase.findById(id);
        return ResponseEntity.ok(FactionResponse.from(faction, findLinkedEntitiesUseCase.findLinks(EntityType.FACTION, faction.getId())));
    }
}
