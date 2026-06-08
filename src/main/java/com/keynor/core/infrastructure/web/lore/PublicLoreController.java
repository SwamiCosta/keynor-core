package com.keynor.core.infrastructure.web.lore;

import com.keynor.core.application.dto.lore.LoreResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.lore.FindAllLoreUseCase;
import com.keynor.core.domain.port.in.lore.FindLoreByIdUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/lore")
public class PublicLoreController {

    private final FindAllLoreUseCase findAllLoreUseCase;
    private final FindLoreByIdUseCase findLoreByIdUseCase;

    public PublicLoreController(
            FindAllLoreUseCase findAllLoreUseCase,
            FindLoreByIdUseCase findLoreByIdUseCase) {
        this.findAllLoreUseCase = findAllLoreUseCase;
        this.findLoreByIdUseCase = findLoreByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LoreResponse>> findAll(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of(),
                tags != null ? tags : List.of());
        var result = findAllLoreUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, LoreResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoreResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(LoreResponse.from(findLoreByIdUseCase.findById(id)));
    }
}
