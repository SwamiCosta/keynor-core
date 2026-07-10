package com.keynor.core.infrastructure.web.character;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.port.in.character.FindAllCharactersUseCase;
import com.keynor.core.domain.port.in.character.FindCharacterByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/characters")
public class PublicCharacterController {

    private final FindAllCharactersUseCase findAllCharactersUseCase;
    private final FindCharacterByIdUseCase findCharacterByIdUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public PublicCharacterController(
            FindAllCharactersUseCase findAllCharactersUseCase,
            FindCharacterByIdUseCase findCharacterByIdUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.findAllCharactersUseCase = findAllCharactersUseCase;
        this.findCharacterByIdUseCase = findCharacterByIdUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CharacterResponse>> findAll(
            @RequestParam String language,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = new EntityFilter(
                LanguageRequestParser.parse(language),
                List.of(EntityStatus.CANON),
                categories != null ? categories : List.of());
        var result = findAllCharactersUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                character -> CharacterResponse.from(character, findLinkedEntitiesUseCase.findLinks(EntityType.CHARACTER, character.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterResponse> findById(@PathVariable UUID id) {
        var character = findCharacterByIdUseCase.findById(id);
        return ResponseEntity.ok(CharacterResponse.from(character, findLinkedEntitiesUseCase.findLinks(EntityType.CHARACTER, character.getId())));
    }
}
