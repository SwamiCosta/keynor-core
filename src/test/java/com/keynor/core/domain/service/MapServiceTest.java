package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.map.GameMap;
import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.port.out.MapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock
    private MapRepository mapRepository;

    private MapService mapService;

    @BeforeEach
    void setUp() {
        mapService = new MapService(mapRepository);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        GameMap gameMap = new GameMap("map-1", "World Map", MapType.NAVIGABLE, "world.png", List.of("era-1"));
        when(mapRepository.findAll()).thenReturn(List.of(gameMap));

        List<GameMap> result = mapService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("map-1");
        verify(mapRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoMapsExist() {
        when(mapRepository.findAll()).thenReturn(List.of());

        List<GameMap> result = mapService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnMap_whenMapExists() {
        GameMap gameMap = new GameMap("map-1", "World Map", MapType.NAVIGABLE, "world.png", List.of("era-1"));
        when(mapRepository.findById("map-1")).thenReturn(Optional.of(gameMap));

        GameMap result = mapService.findById("map-1");

        assertThat(result.getId()).isEqualTo("map-1");
        assertThat(result.getName()).isEqualTo("World Map");
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenMapDoesNotExist() {
        when(mapRepository.findById("unknown-map")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapService.findById("unknown-map"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByEraId_shouldDelegateToRepository() {
        GameMap gameMap = new GameMap("map-1", "World Map", MapType.NAVIGABLE, "world.png", List.of("era-1"));
        when(mapRepository.findByEraId("era-1")).thenReturn(List.of(gameMap));

        List<GameMap> result = mapService.findByEraId("era-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("map-1");
        verify(mapRepository).findByEraId("era-1");
    }

    @Test
    void findByEraId_shouldReturnEmptyList_whenNoMapsForEra() {
        when(mapRepository.findByEraId("era-99")).thenReturn(List.of());

        List<GameMap> result = mapService.findByEraId("era-99");

        assertThat(result).isEmpty();
    }
}
