package com.keynor.core.infrastructure.web.map;

import com.keynor.core.application.dto.map.MapPinResponse;
import com.keynor.core.domain.model.map.MapPin;
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
        return new MapPin(UUID.randomUUID(), MAP_ID, EntityType.CHARACTER, entityId, 0.5, 0.5, Instant.now());
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
}
