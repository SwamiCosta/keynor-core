package com.keynor.core.infrastructure.web.map;

import com.keynor.core.application.dto.map.MapPinResponse;
import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.map.PinShape;
import com.keynor.core.domain.model.shared.EntityLinkSummary;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.in.map.FindMapPinsUseCase;
import com.keynor.core.domain.port.in.shared.FindEntitySummaryUseCase;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicMapPinControllerTest {

    private static final String MAP_ID = "world-map";

    @Mock
    private FindMapPinsUseCase findMapPinsUseCase;

    @Mock
    private FindEntitySummaryUseCase findEntitySummaryUseCase;

    private PublicMapPinController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicMapPinController(findMapPinsUseCase, findEntitySummaryUseCase);
    }

    private MapPin aPin(UUID entityId) {
        return new MapPin(UUID.randomUUID(), MAP_ID, EntityType.CHARACTER, entityId, null, PinShape.DEFAULT, 0.5, 0.5, Instant.now());
    }

    @Test
    void findByMap_shouldReturnPin_whenTargetIsNotCommon() {
        UUID entityId = UUID.randomUUID();
        MapPin pin = aPin(entityId);
        EntityLinkSummary summary = new EntityLinkSummary(EntityType.CHARACTER, entityId, "Araveth", EntityStatus.CANON, false, false);
        when(findMapPinsUseCase.findByMapId(MAP_ID)).thenReturn(List.of(pin));
        when(findEntitySummaryUseCase.findSummary(EntityType.CHARACTER, entityId)).thenReturn(Optional.of(summary));

        var response = controller.findByMap(MAP_ID);

        List<MapPinResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).entity().name()).isEqualTo("Araveth");
        assertThat(body.get(0).name()).isEqualTo("Araveth");
        assertThat(body.get(0).shape()).isEqualTo(PinShape.DEFAULT);
    }

    @Test
    void findByMap_shouldIncludeStarShape_whenPinHasStarShape() {
        UUID entityId = UUID.randomUUID();
        MapPin pin = new MapPin(UUID.randomUUID(), MAP_ID, EntityType.CHARACTER, entityId, null, PinShape.STAR, 0.5, 0.5, Instant.now());
        EntityLinkSummary summary = new EntityLinkSummary(EntityType.CHARACTER, entityId, "Araveth", EntityStatus.CANON, false, false);
        when(findMapPinsUseCase.findByMapId(MAP_ID)).thenReturn(List.of(pin));
        when(findEntitySummaryUseCase.findSummary(EntityType.CHARACTER, entityId)).thenReturn(Optional.of(summary));

        var response = controller.findByMap(MAP_ID);

        List<MapPinResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get(0).shape()).isEqualTo(PinShape.STAR);
    }

    @Test
    void findByMap_shouldDropPin_whenTargetIsCommon() {
        UUID entityId = UUID.randomUUID();
        MapPin pin = aPin(entityId);
        EntityLinkSummary commonSummary = new EntityLinkSummary(EntityType.CHARACTER, entityId, "Ordinary Villager", EntityStatus.CANON, false, true);
        when(findMapPinsUseCase.findByMapId(MAP_ID)).thenReturn(List.of(pin));
        when(findEntitySummaryUseCase.findSummary(EntityType.CHARACTER, entityId)).thenReturn(Optional.of(commonSummary));

        var response = controller.findByMap(MAP_ID);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void findByMap_shouldIncludePinWithItsOwnName_whenPinHasNoLinkedEntity() {
        MapPin pin = new MapPin(UUID.randomUUID(), MAP_ID, null, null, "Uncharted Ruins", PinShape.DEFAULT, 0.3, 0.7, Instant.now());
        when(findMapPinsUseCase.findByMapId(MAP_ID)).thenReturn(List.of(pin));

        var response = controller.findByMap(MAP_ID);

        List<MapPinResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).name()).isEqualTo("Uncharted Ruins");
        assertThat(body.get(0).entity()).isNull();
    }

    @Test
    void findByMap_shouldSuppressName_whenTargetIsHiddenAndStillLocked() {
        UUID entityId = UUID.randomUUID();
        MapPin pin = new MapPin(UUID.randomUUID(), MAP_ID, EntityType.CHARACTER, entityId, "A curious marker", PinShape.DEFAULT, 0.5, 0.5, Instant.now());
        EntityLinkSummary hiddenSummary = new EntityLinkSummary(EntityType.CHARACTER, entityId, "Secret Vault", EntityStatus.CANON, true, false);
        when(findMapPinsUseCase.findByMapId(MAP_ID)).thenReturn(List.of(pin));
        when(findEntitySummaryUseCase.findSummary(EntityType.CHARACTER, entityId)).thenReturn(Optional.of(hiddenSummary));

        var response = controller.findByMap(MAP_ID);

        List<MapPinResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).name()).isNull();
        assertThat(body.get(0).entity().hidden()).isTrue();
    }
}
