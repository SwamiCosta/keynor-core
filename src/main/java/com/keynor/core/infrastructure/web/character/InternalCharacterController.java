package com.keynor.core.infrastructure.web.character;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.character.CreateCharacterRequest;
import com.keynor.core.application.dto.character.UpdateCharacterRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.character.*;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import com.keynor.core.infrastructure.web.shared.EntityStatusRequestParser;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/characters")
public class InternalCharacterController {

    private final CreateCharacterUseCase createCharacterUseCase;
    private final UpdateCharacterUseCase updateCharacterUseCase;
    private final ChangeCharacterStatusUseCase changeCharacterStatusUseCase;
    private final DeleteCharacterUseCase deleteCharacterUseCase;
    private final FindCharacterByIdUseCase findCharacterByIdUseCase;
    private final FindAllCharactersUseCase findAllCharactersUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public InternalCharacterController(
            CreateCharacterUseCase createCharacterUseCase,
            UpdateCharacterUseCase updateCharacterUseCase,
            ChangeCharacterStatusUseCase changeCharacterStatusUseCase,
            DeleteCharacterUseCase deleteCharacterUseCase,
            FindCharacterByIdUseCase findCharacterByIdUseCase,
            FindAllCharactersUseCase findAllCharactersUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.createCharacterUseCase = createCharacterUseCase;
        this.updateCharacterUseCase = updateCharacterUseCase;
        this.changeCharacterStatusUseCase = changeCharacterStatusUseCase;
        this.deleteCharacterUseCase = deleteCharacterUseCase;
        this.findCharacterByIdUseCase = findCharacterByIdUseCase;
        this.findAllCharactersUseCase = findAllCharactersUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CharacterResponse>> findAll(
            @RequestParam String language,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityFilter filter = buildFilter(language, statuses, categories);
        var result = findAllCharactersUseCase.findAll(filter, new PageRequest(page, size));
        return ResponseEntity.ok(PagedResponse.from(result,
                character -> CharacterResponse.from(character, findLinkedEntitiesUseCase.findLinks(EntityType.CHARACTER, character.getId()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterResponse> findById(@PathVariable UUID id) {
        var character = findCharacterByIdUseCase.findById(id);
        return ResponseEntity.ok(CharacterResponse.from(character, findLinkedEntitiesUseCase.findLinks(EntityType.CHARACTER, character.getId())));
    }

    @PostMapping
    public ResponseEntity<CharacterResponse> create(@Valid @RequestBody CreateCharacterRequest request) {
        List<EntityLinkRef> links = toLinkRefs(request.links());
        EntityStatus initialStatus = EntityStatusRequestParser.parseCreationStatus(request.status());
        var command = new CreateCharacterUseCase.Command(
                request.name(),
                request.summary(),
                request.body(),
                request.images() != null ? request.images() : List.of(),
                parseCategories(request.categories()),
                buildTimeline(request.timelineFoundedEra(), request.timelineDestroyedEra()),
                initialStatus,
                LanguageRequestParser.parse(request.language()),
                request.translationGroupId(),
                request.versionGroupId(),
                links,
                request.hidden(),
                request.riddleText(),
                request.password());
        var created = createCharacterUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CharacterResponse.from(created, findLinkedEntitiesUseCase.findLinks(EntityType.CHARACTER, created.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateCharacterRequest request) {
        List<EntityLinkRef> links = toLinkRefs(request.links());
        var command = new UpdateCharacterUseCase.Command(
                request.name(),
                request.summary(),
                request.body(),
                request.images() != null ? request.images() : List.of(),
                parseCategories(request.categories()),
                buildTimeline(request.timelineFoundedEra(), request.timelineDestroyedEra()),
                links,
                request.hidden(), request.riddleText(), request.password());
        var updated = updateCharacterUseCase.update(id, command);
        return ResponseEntity.ok(CharacterResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.CHARACTER, updated.getId())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CharacterResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        EntityStatus newStatus = EntityStatus.valueOf(request.status().toUpperCase());
        var updated = changeCharacterStatusUseCase.changeStatus(id, newStatus);
        return ResponseEntity.ok(CharacterResponse.from(updated, findLinkedEntitiesUseCase.findLinks(EntityType.CHARACTER, updated.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteCharacterUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityLinkRef> toLinkRefs(List<com.keynor.core.application.dto.shared.EntityLinkRequest> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream()
                .map(link -> new EntityLinkRef(EntityType.valueOf(link.targetType().toUpperCase()), link.targetId()))
                .toList();
    }

    private EntityFilter buildFilter(String language, List<String> statuses, List<String> categories) {
        List<EntityStatus> parsedStatuses = statuses != null
                ? statuses.stream().map(s -> EntityStatus.valueOf(s.toUpperCase())).toList()
                : List.of();
        return new EntityFilter(LanguageRequestParser.parse(language), parsedStatuses, categories != null ? categories : List.of(), false);
    }

    private List<CharacterCategory> parseCategories(List<String> categories) {
        return categories.stream().map(c -> CharacterCategory.valueOf(c.toUpperCase())).toList();
    }

    private Timeline buildTimeline(String founded, String destroyed) {
        if (founded == null && destroyed == null) return null;
        return new Timeline(founded, destroyed);
    }
}
