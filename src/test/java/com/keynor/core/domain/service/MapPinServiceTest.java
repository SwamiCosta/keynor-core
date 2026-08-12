package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.map.GameMap;
import com.keynor.core.domain.model.map.MapPin;
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

        var command = new CreateMapPinUseCase.Command(MAP_ID, EntityType.CHARACTER, ENTITY_ID, 0.5, 0.5);
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

        var command = new CreateMapPinUseCase.Command(MAP_ID, EntityType.CHARACTER, ENTITY_ID, 0.5, 0.5);

        assertThatThrownBy(() -> mapPinService.create(command))
                .isInstanceOf(DuplicateEntityNameException.class);
    }

    @Test
    void create_shouldThrow_whenLinkedEntityDoesNotExist() {
        when(mapRepository.findById(MAP_ID)).thenReturn(Optional.of(aMap()));
        when(universeEntityLookupRepository.findSummary(EntityType.CHARACTER, ENTITY_ID))
                .thenReturn(Optional.empty());

        var command = new CreateMapPinUseCase.Command(MAP_ID, EntityType.CHARACTER, ENTITY_ID, 0.5, 0.5);

        assertThatThrownBy(() -> mapPinService.create(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldRepositionPin_whenPinBelongsToGivenMap() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, 0.1, 0.1, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(mapPinRepository.save(any(MapPin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MapPin result = mapPinService.update(MAP_ID, pinId, new UpdateMapPinUseCase.Command(0.8, 0.9));

        assertThat(result.getNormalizedX()).isEqualTo(0.8);
        assertThat(result.getNormalizedY()).isEqualTo(0.9);
        assertThat(result.getId()).isEqualTo(pinId);
    }

    @Test
    void update_shouldThrow_whenPinBelongsToDifferentMap() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, "other-map", EntityType.CHARACTER, ENTITY_ID, 0.1, 0.1, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));

        assertThatThrownBy(() -> mapPinService.update(MAP_ID, pinId, new UpdateMapPinUseCase.Command(0.8, 0.9)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_shouldRemovePin_whenPinBelongsToGivenMap() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, 0.5, 0.5, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));

        mapPinService.delete(MAP_ID, pinId);

        verify(mapPinRepository).deleteById(pinId);
    }

    @Test
    void delete_shouldThrow_whenPinBelongsToDifferentMap() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, "other-map", EntityType.CHARACTER, ENTITY_ID, 0.5, 0.5, Instant.now());
        when(mapPinRepository.findById(pinId)).thenReturn(Optional.of(pin));

        assertThatThrownBy(() -> mapPinService.delete(MAP_ID, pinId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByMapId_shouldReturnPins_whenMapExists() {
        UUID pinId = UUID.randomUUID();
        MapPin pin = new MapPin(pinId, MAP_ID, EntityType.CHARACTER, ENTITY_ID, 0.5, 0.5, Instant.now());
        when(mapRepository.findById(MAP_ID)).thenReturn(Optional.of(aMap()));
        when(mapPinRepository.findByMapId(MAP_ID)).thenReturn(List.of(pin));

        List<MapPin> result = mapPinService.findByMapId(MAP_ID);

        assertThat(result).containsExactly(pin);
    }
}
