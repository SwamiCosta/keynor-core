package com.keynor.core.infrastructure.web.era;

import com.keynor.core.application.dto.era.CreateEraRequest;
import com.keynor.core.application.dto.era.UpdateEraRequest;
import com.keynor.core.application.dto.shared.EntityLinkRequest;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.port.in.era.CreateEraUseCase;
import com.keynor.core.domain.port.in.era.UpdateEraUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalEraControllerTest {

    @Mock private CreateEraUseCase createEraUseCase;
    @Mock private UpdateEraUseCase updateEraUseCase;
    @Mock private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private InternalEraController controller;

    private Era buildEra(UUID id) {
        Instant now = Instant.now();
        return new Era(id, "Age of Creation", 1, EraType.ERA, null, "The first age", now, now, Language.EN, UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        controller = new InternalEraController(createEraUseCase, updateEraUseCase, findLinkedEntitiesUseCase);
        org.mockito.Mockito.lenient().when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());
    }

    @Test
    void create_shouldReturn201AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(createEraUseCase.create(any())).thenReturn(buildEra(id));

        var request = new CreateEraRequest("Age of Creation", 1, "ERA", null, "The first age", "en", null, null);

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Age of Creation");
        verify(createEraUseCase).create(any());
    }

    @Test
    void create_shouldPassResolvedLinkRefs_toCommand_whenLinksProvided() {
        UUID id = UUID.randomUUID();
        UUID loreTargetId = UUID.randomUUID();
        when(createEraUseCase.create(any())).thenReturn(buildEra(id));

        var request = new CreateEraRequest("Age of Creation", 1, "ERA", null, "The first age", "en", null,
                List.of(new EntityLinkRequest("LORE", loreTargetId)));

        controller.create(request);

        ArgumentCaptor<CreateEraUseCase.Command> captor = ArgumentCaptor.forClass(CreateEraUseCase.Command.class);
        verify(createEraUseCase).create(captor.capture());
        assertThat(captor.getValue().links()).containsExactly(new EntityLinkRef(EntityType.LORE, loreTargetId));
    }

    @Test
    void update_shouldReturn200AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(updateEraUseCase.update(eq(id), any())).thenReturn(buildEra(id));

        var request = new UpdateEraRequest("Age of Creation", 1, "ERA", null, "The first age", null);

        var response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Age of Creation");
        verify(updateEraUseCase).update(eq(id), any());
    }

    @Test
    void update_shouldPassResolvedLinkRefs_toCommand_whenLinksProvided() {
        UUID id = UUID.randomUUID();
        UUID loreTargetId = UUID.randomUUID();
        when(updateEraUseCase.update(eq(id), any())).thenReturn(buildEra(id));

        var request = new UpdateEraRequest("Age of Creation", 1, "ERA", null, "The first age",
                List.of(new EntityLinkRequest("LORE", loreTargetId)));

        controller.update(id, request);

        ArgumentCaptor<UpdateEraUseCase.Command> captor = ArgumentCaptor.forClass(UpdateEraUseCase.Command.class);
        verify(updateEraUseCase).update(eq(id), captor.capture());
        assertThat(captor.getValue().links()).containsExactly(new EntityLinkRef(EntityType.LORE, loreTargetId));
    }
}
