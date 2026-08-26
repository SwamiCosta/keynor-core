package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.map.GameMap;
import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.map.PinShape;
import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.model.shared.EntityLinkSummary;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.in.map.CreateMapPinUseCase;
import com.keynor.core.domain.port.in.map.UpdateMapPinUseCase;
import com.keynor.core.domain.port.out.MapPinRepository;
import com.keynor.core.domain.port.out.MapRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapPinServiceTest {

    @Mock
    private MapPinRepository mapPinRepository;

    @Mock
    private MapRepository mapRepository;

    @Mock
    private UniverseEntityLookupRepository universeEntityLookupRepository;

    private MapPinService mapPinService;

    private static final String MAP_ID = "world-map";
    private static final UUID ENTITY_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mapPinService = new MapPinService(mapPinRepository, mapRepository, universeEntityLookupRepository);
    }

    private GameMap aMap() {
        return new GameMap(MAP_ID, "World Map", MapType.NAVIGABLE, "world.png", List.of());
    }

    private EntityLinkSummary aCharacterSummary() {
        return new EntityLinkSummary(EntityType.CHARACTER, ENTITY_ID, "Aroneus", EntityStatus.CANON, false, false);
    }

    @Test
    void create_shouldReturnSavedPin_whenMapAndEntityExistAndNoDuplicate() {
        when(mapRepository.findById(MAP_ID)).thenReturn(Optional.of(aMap()));
        when(universeEntityLookupRepository.findSummary(EntityType.CHARACTER, ENTITY_ID))
                .thenReturn(Optional.of(aCharacterSummary()));
        when(mapPinRepository.existsByMapIdAndEntityTypeAndEntityId(MAP_ID, EntityType.CHARACTER, ENTITY_ID))
                .thenReturn(false);
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new CreateMapPinUseCase.Command(MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.5, 0.5);
        MapPin result = mapPinService.create(command);

        assertThat(result.getMapId()).isEqualTo(MAP_ID);
        assertThat(result.getEntityType()).isEqualTo(EntityType.CHARACTER);
        assertThat(result.getEntityId()).isEqualTo(ENTITY_ID);
        verify(mapPinRepository).save(any(MapPin.class));
    }

    @Test
    void create_shouldThrow_whenPinAlreadyExistsForSameEntityOnSameMap() {
        when(mapRepository.findById(MAP_ID)).thenReturn(Optional.of(aMap()));
        when(universeEntityLookupRepository.findSummary(EntityType.CHARACTER, ENTITY_ID))
                .thenReturn(Optional.of(aCharacterSummary()));
        when(mapPinRepository.existsByMapIdAndEntityTypeAndEntityId(MAP_ID, EntityType.CHARACTER, ENTITY_ID))
                .thenReturn(true);

        var command = new CreateMapPinUseCase.Command(MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.5, 0.5);

        assertThatThrownBy(() -> mapPinService.create(command))
                .isInstanceOf(DuplicateEntityNameException.class);
    }

    @Test
    void create_shouldThrow_whenLinkedEntityDoesNotExist() {
        when(mapRepository.findById(MAP_ID)).thenReturn(Optional.of(aMap()));
        when(universeEntityLookupRepository.findSummary(EntityType.CHARACTER, ENTITY_ID))
                .thenReturn(Optional.empty());

        var command = new CreateMapPinUseCase.Command(MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.5, 0.5);

        assertThatThrownBy(() -> mapPinService.create(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_shouldReturnSavedPin_whenNoEntityGivenButNamePresent() {
        when(mapRepository.findById(MAP_ID)).thenReturn(Optional.of(aMap()));
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new CreateMapPinUseCase.Command(MAP_ID, null, null, "Uncharted Ruins", PinShape.DEFAULT, 0.4, 0.6);
        MapPin result = mapPinService.create(command);

        assertThat(result.getEntityType()).isNull();
        assertThat(result.getEntityId()).isNull();
        assertThat(result.getName()).isEqualTo("Uncharted Ruins");
    }

    @Test
    void create_shouldThrow_whenNoEntityAndNoNameGiven() {
        when(mapRepository.findById(MAP_ID)).thenReturn(Optional.of(aMap()));

        var command = new CreateMapPinUseCase.Command(MAP_ID, null, null, null, PinShape.DEFAULT, 0.4, 0.6);

        assertThatThrownBy(() -> mapPinService.create(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_shouldRepositionPin_whenPinBelongsToGivenMap() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.1, 0.1, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MapPin result = mapPinService.update(MAP_ID, pinId, new UpdateMapPinUseCase.Command(0.8, 0.9, null, null, null, null));

        assertThat(result.getNormalizedX()).isEqualTo(0.8);
        assertThat(result.getNormalizedY()).isEqualTo(0.9);
        assertThat(result.getId()).isEqualTo(pinId);
    }

    // Regression: a drag-only reposition never resends `name` (see
    // MapArea.tsx's handleMovePin). Before this fix, an omitted name was
    // treated as "clear the override" -- fatal for an entity-less pin, whose
    // name is its only identifier, and a silent data-loss bug for any
    // entity-linked pin with a custom name.
    @Test
    void update_shouldPreserveExistingName_whenNameOmittedOnPinWithNoEntity() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, null, null, "Uncharted Ruins", PinShape.DEFAULT, 0.2, 0.2, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MapPin result = mapPinService.update(MAP_ID, pinId, new UpdateMapPinUseCase.Command(0.7, 0.8, null, null, null, null));

        assertThat(result.getName()).isEqualTo("Uncharted Ruins");
        assertThat(result.getNormalizedX()).isEqualTo(0.7);
        assertThat(result.getNormalizedY()).isEqualTo(0.8);
    }

    @Test
    void update_shouldPreserveExistingShape_whenShapeOmitted() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.STAR, 0.1, 0.1, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MapPin result = mapPinService.update(MAP_ID, pinId, new UpdateMapPinUseCase.Command(0.7, 0.8, null, null, null, null));

        assertThat(result.getShape()).isEqualTo(PinShape.STAR);
    }

    @Test
    void update_shouldChangeShape_whenShapeProvided() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.1, 0.1, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MapPin result = mapPinService.update(MAP_ID, pinId,
                new UpdateMapPinUseCase.Command(0.1, 0.1, null, null, null, PinShape.STAR));

        assertThat(result.getShape()).isEqualTo(PinShape.STAR);
    }

    @Test
    void update_shouldSetCustomName_whenNameProvided() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.1, 0.1, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MapPin result = mapPinService.update(MAP_ID, pinId,
                new UpdateMapPinUseCase.Command(0.1, 0.1, "Renamed Landmark", null, null, null));

        assertThat(result.getName()).isEqualTo("Renamed Landmark");
    }

    @Test
    void update_shouldAttachEntity_whenPinHadNoEntityAndEntityGivenInCommand() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, null, null, "Unmarked Spot", PinShape.DEFAULT, 0.2, 0.2, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(universeEntityLookupRepository.findSummary(EntityType.CHARACTER, ENTITY_ID))
                .thenReturn(Optional.of(aCharacterSummary()));
        when(mapPinRepository.existsByMapIdAndEntityTypeAndEntityIdAndIdNot(MAP_ID, EntityType.CHARACTER, ENTITY_ID, pinId))
                .thenReturn(false);
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MapPin result = mapPinService.update(MAP_ID, pinId,
                new UpdateMapPinUseCase.Command(0.2, 0.2, "Unmarked Spot", EntityType.CHARACTER, ENTITY_ID, null));

        assertThat(result.getEntityType()).isEqualTo(EntityType.CHARACTER);
        assertThat(result.getEntityId()).isEqualTo(ENTITY_ID);
    }

    @Test
    void update_shouldThrow_whenEntityTypeGivenWithoutEntityId() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, null, null, "Unmarked Spot", PinShape.DEFAULT, 0.2, 0.2, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));

        assertThatThrownBy(() -> mapPinService.update(MAP_ID, pinId,
                new UpdateMapPinUseCase.Command(0.2, 0.2, null, EntityType.CHARACTER, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_shouldThrow_whenPinBelongsToDifferentMap() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, "other-map", EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.1, 0.1, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));

        assertThatThrownBy(() -> mapPinService.update(MAP_ID, pinId, new UpdateMapPinUseCase.Command(0.8, 0.9, null, null, null, null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_shouldRemovePin_whenPinBelongsToGivenMap() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.5, 0.5, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));

        mapPinService.delete(MAP_ID, pinId);

        verify(mapPinRepository).deleteById(pinId);
    }

    @Test
    void delete_shouldThrow_whenPinBelongsToDifferentMap() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, "other-map", EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.5, 0.5, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));

        assertThatThrownBy(() -> mapPinService.delete(MAP_ID, pinId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByMapId_shouldReturnPins_whenMapExists() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, null, PinShape.DEFAULT, 0.5, 0.5, Instant.now());
        when(mapRepository.findById(MAP_ID)).thenReturn(Optional.of(aMap()));
        when(mapPinRepository.findByMapId(MAP_ID)).thenReturn(List.of(pin));

        List<MapPin> result = mapPinService.findByMapId(MAP_ID);

        assertThat(result).containsExactly(pin);
    }
}
