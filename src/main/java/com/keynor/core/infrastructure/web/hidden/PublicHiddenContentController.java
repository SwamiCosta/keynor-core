package com.keynor.core.infrastructure.web.hidden;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.event.EventResponse;
import com.keynor.core.application.dto.faction.FactionResponse;
import com.keynor.core.application.dto.hidden.HiddenContentRiddleResponse;
import com.keynor.core.application.dto.hidden.UnlockHiddenContentRequest;
import com.keynor.core.application.dto.hidden.UnlockHiddenContentResponse;
import com.keynor.core.application.dto.item.ItemResponse;
import com.keynor.core.application.dto.lore.LoreResponse;
import com.keynor.core.application.dto.place.PlaceResponse;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.HiddenContentAccessDeniedException;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.in.character.FindCharacterByIdUseCase;
import com.keynor.core.domain.port.in.event.FindEventByIdUseCase;
import com.keynor.core.domain.port.in.faction.FindFactionByIdUseCase;
import com.keynor.core.domain.port.in.item.FindItemByIdUseCase;
import com.keynor.core.domain.port.in.lore.FindLoreByIdUseCase;
import com.keynor.core.domain.port.in.place.FindPlaceByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import com.keynor.core.domain.port.in.shared.HiddenContentAccessUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Serves hidden content -- see root ARCHITECTURE.md, "Cross-Project
 * Feature: Hidden Content & Black Pins". All 6 entity types are wired.
 *
 * Deliberately outside the normal Public*Controller family (does not reuse
 * Public*Controller's own findById): those must keep excluding hidden
 * entities unconditionally, with no token bypass, so a black pin (or a link
 * from another unlocked hidden entity) stays the only route in.
 */
@RestController
@RequestMapping("/api/public/v1/hidden/{entityType}/{entityId}")
public class PublicHiddenContentController {

    private static final String TOKEN_HEADER = "X-Hidden-Unlock-Token";

    private final HiddenContentAccessUseCase hiddenContentAccessUseCase;
    private final FindLoreByIdUseCase findLoreByIdUseCase;
    private final FindCharacterByIdUseCase findCharacterByIdUseCase;
    private final FindPlaceByIdUseCase findPlaceByIdUseCase;
    private final FindFactionByIdUseCase findFactionByIdUseCase;
    private final FindItemByIdUseCase findItemByIdUseCase;
    private final FindEventByIdUseCase findEventByIdUseCase;
    private final FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    public PublicHiddenContentController(
            HiddenContentAccessUseCase hiddenContentAccessUseCase,
            FindLoreByIdUseCase findLoreByIdUseCase,
            FindCharacterByIdUseCase findCharacterByIdUseCase,
            FindPlaceByIdUseCase findPlaceByIdUseCase,
            FindFactionByIdUseCase findFactionByIdUseCase,
            FindItemByIdUseCase findItemByIdUseCase,
            FindEventByIdUseCase findEventByIdUseCase,
            FindLinkedEntitiesUseCase findLinkedEntitiesUseCase) {
        this.hiddenContentAccessUseCase = hiddenContentAccessUseCase;
        this.findLoreByIdUseCase = findLoreByIdUseCase;
        this.findCharacterByIdUseCase = findCharacterByIdUseCase;
        this.findPlaceByIdUseCase = findPlaceByIdUseCase;
        this.findFactionByIdUseCase = findFactionByIdUseCase;
        this.findItemByIdUseCase = findItemByIdUseCase;
        this.findEventByIdUseCase = findEventByIdUseCase;
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
            case PLACE -> {
                Place place = findPlaceByIdUseCase.findById(entityId);
                requireHidden(place.isHidden(), type, entityId);
                yield ResponseEntity.ok(PlaceResponse.from(place, findLinkedEntitiesUseCase.findLinks(EntityType.PLACE, entityId)));
            }
            case FACTION -> {
                Faction faction = findFactionByIdUseCase.findById(entityId);
                requireHidden(faction.isHidden(), type, entityId);
                yield ResponseEntity.ok(FactionResponse.from(faction, findLinkedEntitiesUseCase.findLinks(EntityType.FACTION, entityId)));
            }
            case ITEM -> {
                Item item = findItemByIdUseCase.findById(entityId);
                requireHidden(item.isHidden(), type, entityId);
                yield ResponseEntity.ok(ItemResponse.from(item, findLinkedEntitiesUseCase.findLinks(EntityType.ITEM, entityId)));
            }
            case EVENT -> {
                Event event = findEventByIdUseCase.findById(entityId);
                requireHidden(event.isHidden(), type, entityId);
                yield ResponseEntity.ok(EventResponse.from(event, findLinkedEntitiesUseCase.findLinks(EntityType.EVENT, entityId)));
            }
            // Era has no `hidden` field at all -- it is not a UniverseEntity subclass
            // and can never be hidden content, so this route is never valid for it.
            case ERA -> throw new IllegalArgumentException("Era is not a valid hidden-content entity type");
        };
    }

    private void requireHidden(boolean isHidden, EntityType type, UUID id) {
        if (!isHidden) {
            throw new EntityNotFoundException(type.name(), id);
        }
    }
}
