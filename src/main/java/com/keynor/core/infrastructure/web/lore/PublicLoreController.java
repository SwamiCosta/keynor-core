package com.keynor.core.infrastructure.web.lore;

import com.keynor.core.application.dto.lore.LoreResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.lore.FindAllLoreUseCase;
import com.keynor.core.domain.port.in.lore.FindLoreByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/lore")
public class PublicLoreController {

    private final FindAllLoreUseCase findAllLoreUseCase;
    private final FindLoreByIdUseCase findLoreByIdUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public PublicLoreController(
            FindAllLoreUseCase findAllLoreUseCase,
            FindLoreByIdUseCase findLoreByIdUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.findAllLoreUseCase = findAllLoreUseCase;
        this.findLoreByIdUseCase = findLoreByIdUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LoreResponse>> findAll(
            @RequestParam String language,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                LanguageRequestParser.parse(language),
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of());
        var result = findAllLoreUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                lore -> LoreResponse.from(lore, findLinkedEntitiesUseCase.findLinks(EntityType.LORE, lore.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoreResponse> findById(@PathVariable UUID id) {
        var lore = findLoreByIdUseCase.findById(id);
        return ResponseEntity.ok(LoreResponse.from(lore, findLinkedEntitiesUseCase.findLinks(EntityType.LORE, lore.getId())));
    }
}
