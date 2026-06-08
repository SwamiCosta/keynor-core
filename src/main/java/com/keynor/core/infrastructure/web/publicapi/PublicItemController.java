package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.item.ItemResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.item.FindAllItemsUseCase;
import com.keynor.core.domain.port.in.item.FindItemByIdUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/items")
public class PublicItemController {

    private final FindAllItemsUseCase findAllItemsUseCase;
    private final FindItemByIdUseCase findItemByIdUseCase;

    public PublicItemController(
            FindAllItemsUseCase findAllItemsUseCase,
            FindItemByIdUseCase findItemByIdUseCase) {
        this.findAllItemsUseCase = findAllItemsUseCase;
        this.findItemByIdUseCase = findItemByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ItemResponse>> findAll(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of(),
                tags != null ? tags : List.of());
        var result = findAllItemsUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, ItemResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ItemResponse.from(findItemByIdUseCase.findById(id)));
    }
}
