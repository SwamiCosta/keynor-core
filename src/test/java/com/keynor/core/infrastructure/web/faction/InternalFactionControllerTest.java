package com.keynor.core.infrastructure.web.faction;

import com.keynor.core.application.dto.faction.CreateFactionRequest;
import com.keynor.core.application.dto.faction.FactionResponse;
import com.keynor.core.application.dto.faction.UpdateFactionRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.faction.*;
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
class InternalFactionControllerTest {

    @Mock private CreateFactionUseCase createFactionUseCase;
    @Mock private UpdateFactionUseCase updateFactionUseCase;
    @Mock private ChangeFactionStatusUseCase changeFactionStatusUseCase;
    @Mock private DeleteFactionUseCase deleteFactionUseCase;
    @Mock private FindFactionByIdUseCase findFactionByIdUseCase;
    @Mock private FindAllFactionsUseCase findAllFactionsUseCase;

    private InternalFactionController controller;

    private Faction buildFaction(UUID id) {
        Instant now = Instant.now();
        return new Faction(id, "The Silver Order", "A guild", "Body", List.of("mages"),
                List.of(), List.of(FactionCategory.ORDER), EntityStatus.DRAFT, null, now, now);
    }

    @BeforeEach
    void setUp() {
        controller = new InternalFactionController(
                createFactionUseCase, updateFactionUseCase, changeFactionStatusUseCase,
                deleteFactionUseCase, findFactionByIdUseCase, findAllFactionsUseCase);
    }

    @Test
    void create_shouldReturn201AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(createFactionUseCase.create(any())).thenReturn(buildFaction(id));

        var request = new CreateFactionRequest("The Silver Order", "A guild", "Body",
                List.of("mages"), List.of(), List.of("ORDER"), "era-1", null);

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("The Silver Order");
        verify(createFactionUseCase).create(any());
    }

    @Test
    void create_shouldPassCorrectCommandToUseCase() {
        UUID id = UUID.randomUUID();
        when(createFactionUseCase.create(any())).thenReturn(buildFaction(id));

        var request = new CreateFactionRequest("The Silver Order", null, null,
                List.of(), List.of(), List.of("ORDER"), "era-1", null);

        controller.create(request);

        ArgumentCaptor<CreateFactionUseCase.Command> captor =
                ArgumentCaptor.forClass(CreateFactionUseCase.Command.class);
        verify(createFactionUseCase).create(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("The Silver Order");
        assertThat(captor.getValue().categories()).containsExactly(FactionCategory.ORDER);
    }

    @Test
    void update_shouldReturn200AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(updateFactionUseCase.update(eq(id), any())).thenReturn(buildFaction(id));

        var request = new UpdateFactionRequest("Updated Name", null, null,
                List.of(), List.of(), List.of("ORDER"), "era-1", null);

        var response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(updateFactionUseCase).update(eq(id), any());
    }

    @Test
    void changeStatus_shouldReturn200AndCallUseCase() {
        UUID id = UUID.randomUUID();
        when(changeFactionStatusUseCase.changeStatus(id, EntityStatus.CANON)).thenReturn(buildFaction(id));

        var response = controller.changeStatus(id, new ChangeStatusRequest("CANON"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(changeFactionStatusUseCase).changeStatus(id, EntityStatus.CANON);
    }

    @Test
    void delete_shouldReturn204AndCallUseCase() {
        UUID id = UUID.randomUUID();

        var response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deleteFactionUseCase).delete(id);
    }

    @Test
    void findAll_shouldPassPaginationAndReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        when(findAllFactionsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(buildFaction(id)), 0, 20, 1));

        var response = controller.findAll(null, null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<FactionResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllFactionsUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(0);
        assertThat(pageCaptor.getValue().size()).isEqualTo(20);
    }

    @Test
    void findAll_shouldNotApplyCanonFilter_whenNoStatusProvided() {
        when(findAllFactionsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllFactionsUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).isEmpty();
    }

    @Test
    void findById_shouldDelegateAndMapResult() {
        UUID id = UUID.randomUUID();
        when(findFactionByIdUseCase.findById(id)).thenReturn(buildFaction(id));

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findFactionByIdUseCase).findById(id);
    }
}
