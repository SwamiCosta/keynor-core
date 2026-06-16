package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.faction.FactionResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.faction.FindAllFactionsUseCase;
import com.keynor.core.domain.port.in.faction.FindFactionByIdUseCase;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicFactionControllerTest {

    @Mock
    private FindAllFactionsUseCase findAllFactionsUseCase;

    @Mock
    private FindFactionByIdUseCase findFactionByIdUseCase;

    @Mock
    private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private PublicFactionController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicFactionController(
                findAllFactionsUseCase, findFactionByIdUseCase, findLinkedEntitiesUseCase);
    }

    @Test
    void findAll_shouldAlwaysFilterByCanonStatus() {
        when(findAllFactionsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllFactionsUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).containsExactly(EntityStatus.CANON);
    }

    @Test
    void findAll_shouldPassPaginationParamsToUseCase() {
        when(findAllFactionsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 1, 30, 0));

        controller.findAll(null, null, 1, 30);

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllFactionsUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(1);
        assertThat(pageCaptor.getValue().size()).isEqualTo(30);
    }

    @Test
    void findAll_shouldReturnMappedPagedResponse() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Faction faction = new Faction(id, "The Iron Order", "A guild", "Body", List.of("guild"),
                List.of(), List.of(FactionCategory.ORDER), EntityStatus.CANON, null, now, now);
        when(findAllFactionsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(faction), 0, 20, 1));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findAll(null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<FactionResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        assertThat(body.content()).hasSize(1);
        assertThat(body.content().get(0).id()).isEqualTo(id);
        assertThat(body.content().get(0).name()).isEqualTo("The Iron Order");
    }

    @Test
    void findById_shouldDelegateToUseCaseAndMapResult() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Faction faction = new Faction(id, "The Iron Order", "A guild", "Body", List.of(),
                List.of(), List.of(FactionCategory.GUILD), EntityStatus.CANON, null, now, now);
        when(findFactionByIdUseCase.findById(id)).thenReturn(faction);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findFactionByIdUseCase).findById(id);
    }
}
