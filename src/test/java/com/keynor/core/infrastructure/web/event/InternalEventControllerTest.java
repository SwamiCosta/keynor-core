package com.keynor.core.infrastructure.web.event;

import com.keynor.core.application.dto.event.CreateEventRequest;
import com.keynor.core.application.dto.event.EventResponse;
import com.keynor.core.application.dto.event.UpdateEventRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.event.*;
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
class InternalEventControllerTest {

    @Mock private CreateEventUseCase createEventUseCase;
    @Mock private UpdateEventUseCase updateEventUseCase;
    @Mock private ChangeEventStatusUseCase changeEventStatusUseCase;
    @Mock private DeleteEventUseCase deleteEventUseCase;
    @Mock private FindEventByIdUseCase findEventByIdUseCase;
    @Mock private FindAllEventsUseCase findAllEventsUseCase;

    private InternalEventController controller;

    private Event buildEvent(UUID id) {
        Instant now = Instant.now();
        return new Event(id, "The Battle of Kor", "A decisive battle", "Body",
                List.of("battle"), List.of(), List.of(EventCategory.BATTLE),
                EntityStatus.DRAFT, null, now, now);
    }

    @BeforeEach
    void setUp() {
        controller = new InternalEventController(
                createEventUseCase, updateEventUseCase, changeEventStatusUseCase,
                deleteEventUseCase, findEventByIdUseCase, findAllEventsUseCase);
    }

    @Test
    void create_shouldReturn201AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(createEventUseCase.create(any())).thenReturn(buildEvent(id));

        var request = new CreateEventRequest("The Battle of Kor", "A decisive battle", "Body",
                List.of("battle"), List.of(), List.of("BATTLE"), "era-1", null);

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("The Battle of Kor");
        verify(createEventUseCase).create(any());
    }

    @Test
    void create_shouldPassCorrectCommandToUseCase() {
        UUID id = UUID.randomUUID();
        when(createEventUseCase.create(any())).thenReturn(buildEvent(id));

        var request = new CreateEventRequest("The Battle of Kor", null, null,
                List.of(), List.of(), List.of("BATTLE"), "era-1", null);

        controller.create(request);

        ArgumentCaptor<CreateEventUseCase.Command> captor =
                ArgumentCaptor.forClass(CreateEventUseCase.Command.class);
        verify(createEventUseCase).create(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("The Battle of Kor");
        assertThat(captor.getValue().categories()).containsExactly(EventCategory.BATTLE);
    }

    @Test
    void update_shouldReturn200AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(updateEventUseCase.update(eq(id), any())).thenReturn(buildEvent(id));

        var request = new UpdateEventRequest("Battle Updated", null, null,
                List.of(), List.of(), List.of("BATTLE"), "era-1", null);

        var response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(updateEventUseCase).update(eq(id), any());
    }

    @Test
    void changeStatus_shouldReturn200AndCallUseCase() {
        UUID id = UUID.randomUUID();
        when(changeEventStatusUseCase.changeStatus(id, EntityStatus.CANON)).thenReturn(buildEvent(id));

        var response = controller.changeStatus(id, new ChangeStatusRequest("CANON"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(changeEventStatusUseCase).changeStatus(id, EntityStatus.CANON);
    }

    @Test
    void delete_shouldReturn204AndCallUseCase() {
        UUID id = UUID.randomUUID();

        var response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deleteEventUseCase).delete(id);
    }

    @Test
    void findAll_shouldPassPaginationAndReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        when(findAllEventsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(buildEvent(id)), 0, 20, 1));

        var response = controller.findAll(null, null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<EventResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllEventsUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(0);
        assertThat(pageCaptor.getValue().size()).isEqualTo(20);
    }

    @Test
    void findAll_shouldNotApplyCanonFilter_whenNoStatusProvided() {
        when(findAllEventsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllEventsUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).isEmpty();
    }

    @Test
    void findById_shouldDelegateAndMapResult() {
        UUID id = UUID.randomUUID();
        when(findEventByIdUseCase.findById(id)).thenReturn(buildEvent(id));

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findEventByIdUseCase).findById(id);
    }
}
