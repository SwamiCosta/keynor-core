package com.keynor.core.infrastructure.web.place;

import com.keynor.core.application.dto.place.CreatePlaceRequest;
import com.keynor.core.application.dto.place.PlaceResponse;
import com.keynor.core.application.dto.place.UpdatePlaceRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.place.PlaceCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.place.*;
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
class InternalPlaceControllerTest {

    @Mock private CreatePlaceUseCase createPlaceUseCase;
    @Mock private UpdatePlaceUseCase updatePlaceUseCase;
    @Mock private ChangePlaceStatusUseCase changePlaceStatusUseCase;
    @Mock private DeletePlaceUseCase deletePlaceUseCase;
    @Mock private FindPlaceByIdUseCase findPlaceByIdUseCase;
    @Mock private FindAllPlacesUseCase findAllPlacesUseCase;

    private InternalPlaceController controller;

    private Place buildPlace(UUID id) {
        Instant now = Instant.now();
        return new Place(id, "Thornvale", "A city", "Body", List.of("city"),
                List.of(), List.of(PlaceCategory.CITY), MapType.NAVIGABLE,
                EntityStatus.DRAFT, null, now, now);
    }

    @BeforeEach
    void setUp() {
        controller = new InternalPlaceController(
                createPlaceUseCase, updatePlaceUseCase, changePlaceStatusUseCase,
                deletePlaceUseCase, findPlaceByIdUseCase, findAllPlacesUseCase);
    }

    @Test
    void create_shouldReturn201AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(createPlaceUseCase.create(any())).thenReturn(buildPlace(id));

        var request = new CreatePlaceRequest("Thornvale", "A city", "Body",
                List.of("city"), List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null);

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Thornvale");
        verify(createPlaceUseCase).create(any());
    }

    @Test
    void create_shouldPassMapTypeToCommand() {
        UUID id = UUID.randomUUID();
        when(createPlaceUseCase.create(any())).thenReturn(buildPlace(id));

        var request = new CreatePlaceRequest("Thornvale", null, null,
                List.of(), List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null);

        controller.create(request);

        ArgumentCaptor<CreatePlaceUseCase.Command> captor =
                ArgumentCaptor.forClass(CreatePlaceUseCase.Command.class);
        verify(createPlaceUseCase).create(captor.capture());
        assertThat(captor.getValue().mapType()).isEqualTo(MapType.NAVIGABLE);
    }

    @Test
    void update_shouldReturn200AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(updatePlaceUseCase.update(eq(id), any())).thenReturn(buildPlace(id));

        var request = new UpdatePlaceRequest("Thornvale Updated", null, null,
                List.of(), List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null);

        var response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(updatePlaceUseCase).update(eq(id), any());
    }

    @Test
    void changeStatus_shouldReturn200AndCallUseCase() {
        UUID id = UUID.randomUUID();
        when(changePlaceStatusUseCase.changeStatus(id, EntityStatus.CANON)).thenReturn(buildPlace(id));

        var response = controller.changeStatus(id, new ChangeStatusRequest("CANON"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(changePlaceStatusUseCase).changeStatus(id, EntityStatus.CANON);
    }

    @Test
    void delete_shouldReturn204AndCallUseCase() {
        UUID id = UUID.randomUUID();

        var response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deletePlaceUseCase).delete(id);
    }

    @Test
    void findAll_shouldPassPaginationAndReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        when(findAllPlacesUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(buildPlace(id)), 0, 20, 1));

        var response = controller.findAll(null, null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<PlaceResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllPlacesUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(0);
        assertThat(pageCaptor.getValue().size()).isEqualTo(20);
    }

    @Test
    void findAll_shouldNotApplyCanonFilter_whenNoStatusProvided() {
        when(findAllPlacesUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllPlacesUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).isEmpty();
    }

    @Test
    void findById_shouldDelegateAndMapResult() {
        UUID id = UUID.randomUUID();
        when(findPlaceByIdUseCase.findById(id)).thenReturn(buildPlace(id));

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findPlaceByIdUseCase).findById(id);
    }
}
