package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.faction.FactionResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.faction.FindAllFactionsUseCase;
import com.keynor.core.domain.port.in.faction.FindFactionByIdUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/factions")
public class PublicFactionController {

    private final FindAllFactionsUseCase findAllFactionsUseCase;
    private final FindFactionByIdUseCase findFactionByIdUseCase;

    public PublicFactionController(
            FindAllFactionsUseCase findAllFactionsUseCase,
            FindFactionByIdUseCase findFactionByIdUseCase) {
        this.findAllFactionsUseCase = findAllFactionsUseCase;
        this.findFactionByIdUseCase = findFactionByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<FactionResponse>> findAll(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of(),
                tags != null ? tags : List.of());
        var result = findAllFactionsUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, FactionResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(FactionResponse.from(findFactionByIdUseCase.findById(id)));
    }
}
