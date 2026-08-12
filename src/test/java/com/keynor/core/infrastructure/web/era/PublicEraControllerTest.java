package com.keynor.core.infrastructure.web.era;

import com.keynor.core.application.dto.era.EraResponse;
import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.shared.EntityLinkSummary;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.era.EraImportance;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.port.in.era.FindAllErasUseCase;
import com.keynor.core.domain.port.in.era.FindEraByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicEraControllerTest {

    @Mock
    private FindAllErasUseCase findAllErasUseCase;

    @Mock
    private FindEraByIdUseCase findEraByIdUseCase;

    @Mock
    private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private PublicEraController controller;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        controller = new PublicEraController(findAllErasUseCase, findEraByIdUseCase, findLinkedEntitiesUseCase);
    }

    @Test
    void findAll_shouldReturnMixedErasAndPoints_inOrderIndexSequence() {
        UUID eraId = UUID.randomUUID();
        UUID pointId = UUID.randomUUID();
        UUID era2Id = UUID.randomUUID();

        Era eraInterval = new Era(eraId, "Age of Creation", 1, EraType.ERA, null, "The first age", NOW, NOW, Language.EN, UUID.randomUUID());
        Era temporalPoint = new Era(pointId, "The Great Sundering", 2, EraType.POINT, EraImportance.MAJOR, "Cataclysmic event", NOW, NOW, Language.EN, UUID.randomUUID());
        Era eraInterval2 = new Era(era2Id, "Age of Silence", 3, EraType.ERA, null, "Age of quiet", NOW, NOW, Language.EN, UUID.randomUUID());

        when(findAllErasUseCase.findAll(Language.EN)).thenReturn(List.of(eraInterval, temporalPoint, eraInterval2));

        var response = controller.findAll("en");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<EraResponse> body = response.getBody();
        assertThat(body).isNotNull().hasSize(3);

        EraResponse firstEntry = body.get(0);
        assertThat(firstEntry.id()).isEqualTo(eraId);
        assertThat(firstEntry.type()).isEqualTo("ERA");
        assertThat(firstEntry.importance()).isNull();
        assertThat(firstEntry.order()).isEqualTo(1);

        EraResponse secondEntry = body.get(1);
        assertThat(secondEntry.id()).isEqualTo(pointId);
        assertThat(secondEntry.type()).isEqualTo("POINT");
        assertThat(secondEntry.importance()).isEqualTo("MAJOR");
        assertThat(secondEntry.order()).isEqualTo(2);

        EraResponse thirdEntry = body.get(2);
        assertThat(thirdEntry.id()).isEqualTo(era2Id);
        assertThat(thirdEntry.type()).isEqualTo("ERA");
        assertThat(thirdEntry.importance()).isNull();
        assertThat(thirdEntry.order()).isEqualTo(3);

        verify(findAllErasUseCase).findAll(Language.EN);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoEntriesExist() {
        when(findAllErasUseCase.findAll(Language.EN)).thenReturn(List.of());

        var response = controller.findAll("en");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void findAll_shouldSetImportanceToNull_forEraTypeEntries() {
        Era era = new Era(UUID.randomUUID(), "Age of Creation", 1, EraType.ERA, null, "The first age", NOW, NOW, Language.EN, UUID.randomUUID());
        when(findAllErasUseCase.findAll(Language.EN)).thenReturn(List.of(era));

        var response = controller.findAll("en");

        assertThat(response.getBody()).isNotNull().hasSize(1);
        assertThat(response.getBody().get(0).importance()).isNull();
    }

    @Test
    void findAll_shouldSetImportance_forPointTypeEntries() {
        Era point = new Era(UUID.randomUUID(), "Minor Event", 1, EraType.POINT, EraImportance.STANDARD, "A minor event", NOW, NOW, Language.EN, UUID.randomUUID());
        when(findAllErasUseCase.findAll(Language.EN)).thenReturn(List.of(point));

        var response = controller.findAll("en");

        assertThat(response.getBody()).isNotNull().hasSize(1);
        assertThat(response.getBody().get(0).importance()).isEqualTo("STANDARD");
    }

    @Test
    void findById_shouldReturnEra_whenFound() {
        UUID id = UUID.randomUUID();
        Era era = new Era(id, "Age of Creation", 1, EraType.ERA, null, "The first age", NOW, NOW, Language.EN, UUID.randomUUID());
        when(findEraByIdUseCase.findById(id)).thenReturn(era);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        assertThat(response.getBody().type()).isEqualTo("ERA");
        assertThat(response.getBody().importance()).isNull();
        verify(findEraByIdUseCase).findById(id);
    }

    @Test
    void findById_shouldReturnTemporalPoint_whenFound() {
        UUID id = UUID.randomUUID();
        Era point = new Era(id, "The Great Sundering", 2, EraType.POINT, EraImportance.MAJOR, "Cataclysmic event", NOW, NOW, Language.EN, UUID.randomUUID());
        when(findEraByIdUseCase.findById(id)).thenReturn(point);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().type()).isEqualTo("POINT");
        assertThat(response.getBody().importance()).isEqualTo("MAJOR");
        verify(findEraByIdUseCase).findById(id);
    }

    @Test
    void findById_shouldIncludeResolvedLinks_whenEraHasEntityLinks() {
        UUID id = UUID.randomUUID();
        UUID loreId = UUID.randomUUID();
        Era era = new Era(id, "Age of Creation", 1, EraType.ERA, null, "The first age", NOW, NOW, Language.EN, UUID.randomUUID());
        when(findEraByIdUseCase.findById(id)).thenReturn(era);
        when(findLinkedEntitiesUseCase.findLinks(EntityType.ERA, id)).thenReturn(
                List.of(new EntityLinkSummary(EntityType.LORE, loreId, "On the Word God", EntityStatus.CANON, false, false)));

        var response = controller.findById(id);

        assertThat(response.getBody()).isNotNull();
        List<LinkedEntityResponse> links = response.getBody().links();
        assertThat(links).hasSize(1);
        assertThat(links.get(0).type()).isEqualTo("LORE");
        assertThat(links.get(0).id()).isEqualTo(loreId);
        assertThat(links.get(0).name()).isEqualTo("On the Word God");
    }
}
