package com.keynor.core.infrastructure.web.map;

import com.keynor.core.application.dto.map.MapResponse;
import com.keynor.core.domain.model.map.GameMap;
import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.port.in.map.FindAllMapsUseCase;
import com.keynor.core.domain.port.in.map.FindMapByIdUseCase;
import com.keynor.core.domain.port.in.map.FindMapsByEraUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicMapControllerTest {

    @Mock
    private FindAllMapsUseCase findAllMapsUseCase;

    @Mock
    private FindMapByIdUseCase findMapByIdUseCase;

    @Mock
    private FindMapsByEraUseCase findMapsByEraUseCase;

    private PublicMapController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicMapController(findAllMapsUseCase, findMapByIdUseCase, findMapsByEraUseCase);
    }

    @Test
    void findAll_shouldDelegateToFindAllUseCase_whenNoEraIdProvided() {
        GameMap gameMap = new GameMap("map-1", "World Map", MapType.NAVIGABLE, "world.png", List.of("era-1"));
        when(findAllMapsUseCase.findAll()).thenReturn(List.of(gameMap));

        var response = controller.findAll(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<MapResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).id()).isEqualTo("map-1");
        assertThat(body.get(0).name()).isEqualTo("World Map");
        verify(findAllMapsUseCase).findAll();
        verifyNoInteractions(findMapsByEraUseCase);
    }

    @Test
    void findAll_shouldDelegateToFindByEraUseCase_whenEraIdProvided() {
        GameMap gameMap = new GameMap("map-2", "Era Map", MapType.ABSTRACT, "era-map.png", List.of("era-2"));
        when(findMapsByEraUseCase.findByEraId("era-2")).thenReturn(List.of(gameMap));

        var response = controller.findAll("era-2");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<MapResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).id()).isEqualTo("map-2");
        verify(findMapsByEraUseCase).findByEraId("era-2");
        verifyNoInteractions(findAllMapsUseCase);
    }

    @Test
    void findById_shouldDelegateToUseCaseAndMapResult() {
        GameMap gameMap = new GameMap("map-1", "World Map", MapType.NAVIGABLE, "world.png", List.of("era-1"));
        when(findMapByIdUseCase.findById("map-1")).thenReturn(gameMap);

        var response = controller.findById("map-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("map-1");
        verify(findMapByIdUseCase).findById("map-1");
    }

    @Test
    void findById_shouldPassStringIdDirectlyToUseCase() {
        GameMap gameMap = new GameMap("map-ancient", "Ancient Realm", MapType.ABSTRACT, "ancient.png", List.of());
        when(findMapByIdUseCase.findById("map-ancient")).thenReturn(gameMap);

        var response = controller.findById("map-ancient");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo("map-ancient");
        verify(findMapByIdUseCase).findById("map-ancient");
    }
}
