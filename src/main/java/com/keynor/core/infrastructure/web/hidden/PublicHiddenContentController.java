package com.keynor.core.infrastructure.web.hidden;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.hidden.HiddenContentRiddleResponse;
import com.keynor.core.application.dto.hidden.UnlockHiddenContentRequest;
import com.keynor.core.application.dto.hidden.UnlockHiddenContentResponse;
import com.keynor.core.application.dto.lore.LoreResponse;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.HiddenContentAccessDeniedException;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.in.character.FindCharacterByIdUseCase;
import com.keynor.core.domain.port.in.lore.FindLoreByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import com.keynor.core.domain.port.in.shared.HiddenContentAccessUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Serves hidden content -- see root ARCHITECTURE.md, "Cross-Project
 * Feature: Hidden Content & Black Pins". Only LORE and CHARACTER are wired
 * so far (the two entity types in the reference example); the other four
 * fall into the default branch below until replicated -- see
 * .claude/skills/hidden-content-implementation.md.
 *
 * Deliberately outside the normal Public*Controller family (does not reuse
 * PublicLoreController/PublicCharacterController's findById): those must
 * keep excluding hidden entities unconditionally, with no token bypass, so
 * a black pin (or a link from another unlocked hidden entity) stays the
 * only route in.
 */
@RestController
@RequestMapping("/api/public/v1/hidden/{entityType}/{entityId}")
public class PublicHiddenContentController {

    private static final String TOKEN_HEADER = "X-Hidden-Unlock-Token";

    private final HiddenContentAccessUseCase hiddenContentAccessUseCase;
    private final FindLoreByIdUseCase findLoreByIdUseCase;
    private final FindCharacterByIdUseCase findCharacterByIdUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public PublicHiddenContentController(
            HiddenContentAccessUseCase hiddenContentAccessUseCase,
            FindLoreByIdUseCase findLoreByIdUseCase,
            FindCharacterByIdUseCase findCharacterByIdUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.hiddenContentAccessUseCase = hiddenContentAccessUseCase;
        this.findLoreByIdUseCase = findLoreByIdUseCase;
        this.findCharacterByIdUseCase = findCharacterByIdUseCase;
        this.findLinkedEntitiesUseCase = findLinkedEntitiesUseCase;
    }

    @GetMapping("/riddle")
    public ResponseEntity<HiddenContentRiddleResponse> findRiddle(
            @PathVariable String entityType, @PathVariable UUID entityId) {
        EntityType type = EntityType.valueOf(entityType.toUpperCase());
        return ResponseEntity.ok(new HiddenContentRiddleResponse(hiddenContentAccessUseCase.findRiddle(type, entityId)));
    }

    @PostMapping("/unlock")
    public ResponseEntity<UnlockHiddenContentResponse> unlock(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @Valid @RequestBody UnlockHiddenContentRequest request,
            @RequestHeader(value = TOKEN_HEADER, required = false) String existingToken) {
        EntityType type = EntityType.valueOf(entityType.toUpperCase());
        var result = hiddenContentAccessUseCase.unlock(type, entityId, request.password(), existingToken);
        return ResponseEntity.ok(new UnlockHiddenContentResponse(result.token(), result.unlockedAll(), result.expiresAt()));
    }

    @GetMapping
    public ResponseEntity<?> findById(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        EntityType type = EntityType.valueOf(entityType.toUpperCase());
        if (!hiddenContentAccessUseCase.hasAccess(token, type, entityId)) {
            throw new HiddenContentAccessDeniedException();
        }
        return switch (type) {
            case LORE -> {
                Lore lore = findLoreByIdUseCase.findById(entityId);
                requireHidden(lore.isHidden(), type, entityId);
                yield ResponseEntity.ok(LoreResponse.from(lore, findLinkedEntitiesUseCase.findLinks(EntityType.LORE, entityId)));
            }
            case CHARACTER -> {
                Character character = findCharacterByIdUseCase.findById(entityId);
                requireHidden(character.isHidden(), type, entityId);
                yield ResponseEntity.ok(CharacterResponse.from(character, findLinkedEntitiesUseCase.findLinks(EntityType.CHARACTER, entityId)));
            }
            default -> throw new EntityNotFoundException("HiddenContent", entityId);
        };
    }

    private void requireHidden(boolean isHidden, EntityType type, UUID id) {
        if (!isHidden) {
            throw new EntityNotFoundException(type.name(), id);
        }
    }
}
