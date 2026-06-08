package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.event.EventResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.event.FindAllEventsUseCase;
import com.keynor.core.domain.port.in.event.FindEventByIdUseCase;
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
class PublicEventControllerTest {

    @Mock
    private FindAllEventsUseCase findAllEventsUseCase;

    @Mock
    private FindEventByIdUseCase findEventByIdUseCase;

    private PublicEventController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicEventController(findAllEventsUseCase, findEventByIdUseCase);
    }

    @Test
    void findAll_shouldAlwaysFilterByCanonStatus() {
        when(findAllEventsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllEventsUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).containsExactly(EntityStatus.CANON);
    }

    @Test
    void findAll_shouldPassPaginationParamsToUseCase() {
        when(findAllEventsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 100, 0));

        controller.findAll(null, null, 0, 100);

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllEventsUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(0);
        assertThat(pageCaptor.getValue().size()).isEqualTo(100);
    }

    @Test
    void findAll_shouldReturnMappedPagedResponse() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Event event = new Event(id, "The First Sundering", "A cataclysmic event", "Body", List.of("war"),
                List.of(), List.of(EventCategory.BATTLE), EntityStatus.CANON, null, now, now);
        when(findAllEventsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(event), 0, 20, 1));

        var response = controller.findAll(null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<EventResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        assertThat(body.content()).hasSize(1);
        assertThat(body.content().get(0).id()).isEqualTo(id);
        assertThat(body.content().get(0).name()).isEqualTo("The First Sundering");
    }

    @Test
    void findById_shouldDelegateToUseCaseAndMapResult() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Event event = new Event(id, "The First Sundering", "A cataclysmic event", "Body", List.of(),
                List.of(), List.of(EventCategory.DIVINE), EntityStatus.CANON, null, now, now);
        when(findEventByIdUseCase.findById(id)).thenReturn(event);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findEventByIdUseCase).findById(id);
    }
}
