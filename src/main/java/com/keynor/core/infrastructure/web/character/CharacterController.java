package com.keynor.core.infrastructure.web.character;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.character.CreateCharacterRequest;
import com.keynor.core.application.dto.character.UpdateCharacterRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.character.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/characters")
public class CharacterController {

    private final CreateCharacterUseCase createCharacterUseCase;
    private final UpdateCharacterUseCase updateCharacterUseCase;
    private final ChangeCharacterStatusUseCase changeCharacterStatusUseCase;
    private final DeleteCharacterUseCase deleteCharacterUseCase;
    private final FindCharacterByIdUseCase findCharacterByIdUseCase;
    private final FindAllCharactersUseCase findAllCharactersUseCase;

    public CharacterController(
            CreateCharacterUseCase createCharacterUseCase,
            UpdateCharacterUseCase updateCharacterUseCase,
            ChangeCharacterStatusUseCase changeCharacterStatusUseCase,
            DeleteCharacterUseCase deleteCharacterUseCase,
            FindCharacterByIdUseCase findCharacterByIdUseCase,
            FindAllCharactersUseCase findAllCharactersUseCase) {
        this.createCharacterUseCase = createCharacterUseCase;
        this.updateCharacterUseCase = updateCharacterUseCase;
        this.changeCharacterStatusUseCase = changeCharacterStatusUseCase;
        this.deleteCharacterUseCase = deleteCharacterUseCase;
        this.findCharacterByIdUseCase = findCharacterByIdUseCase;
        this.findAllCharactersUseCase = findAllCharactersUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CharacterResponse>> findAll(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = buildFilter(statuses, categories, tags);
        var result = findAllCharactersUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result, CharacterResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(CharacterResponse.from(findCharacterByIdUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<CharacterResponse> create(@Valid @RequestBody CreateCharacterRequest request) {
        var command = new CreateCharacterUseCase.Command(
                request.name(),
                request.summary(),
                request.body(),
                request.tags() != null ? request.tags() : List.of(),
                parseCategories(request.categories()),
                buildTimeline(request.timelineFoundedEra(), request.timelineDestroyedEra()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CharacterResponse.from(createCharacterUseCase.create(command)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateCharacterRequest request) {
        var command = new UpdateCharacterUseCase.Command(
                request.name(),
                request.summary(),
                request.body(),
                request.tags() != null ? request.tags() : List.of(),
                parseCategories(request.categories()),
                buildTimeline(request.timelineFoundedEra(), request.timelineDestroyedEra()));
        return ResponseEntity.ok(CharacterResponse.from(updateCharacterUseCase.update(id, command)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CharacterResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        EntityStatus newStatus = EntityStatus.valueOf(request.status().toUpperCase());
        return ResponseEntity.ok(CharacterResponse.from(changeCharacterStatusUseCase.changeStatus(id, newStatus)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteCharacterUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityFilter buildFilter(List<String> statuses, List<String> categories, List<String> tags) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList()
                : List.of();
        return new EntityFilter(parsedStatuses, categories != null ? categories : List.of(), tags != null ? tags : List.of());
    }

    private List<CharacterCategory> parseCategories(List<String> categories) {
        return categories.stream().map(c -> CharacterCategory.valueOf(c.toUpperCase())).toList();
    }

    private Timeline buildTimeline(String founded, String destroyed) {
        if (founded == null && destroyed == null) return null;
        return new Timeline(founded, destroyed);
    }
}
