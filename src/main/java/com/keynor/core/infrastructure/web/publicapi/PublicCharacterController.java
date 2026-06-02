package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.character.FindAllCharactersUseCase;
import com.keynor.core.domain.port.in.character.FindCharacterByIdUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/characters")
public class PublicCharacterController {

    private final FindAllCharactersUseCase findAllCharactersUseCase;
    private final FindCharacterByIdUseCase findCharacterByIdUseCase;

    public PublicCharacterController(
            FindAllCharactersUseCase findAllCharactersUseCase,
            FindCharacterByIdUseCase findCharacterByIdUseCase) {
        this.findAllCharactersUseCase = findAllCharactersUseCase;
        this.findCharacterByIdUseCase = findCharacterByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CharacterResponse>> findAll(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of(),
                tags != null ? tags : List.of());
        var result = findAllCharactersUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, CharacterResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(CharacterResponse.from(findCharacterByIdUseCase.findById(id)));
    }
}
