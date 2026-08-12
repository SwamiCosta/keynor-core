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
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.place.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private InternalPlaceController controller;

    private Place buildPlace(UUID id) {
        Instant now = Instant.now();
        return new Place(id, "Thornvale", "A city", "Body",
                List.of(), List.of(PlaceCategory.CITY), MapType.NAVIGABLE,
                EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
    }

    @BeforeEach
    void setUp() {
        controller = new InternalPlaceController(
                createPlaceUseCase, updatePlaceUseCase, changePlaceStatusUseCase,
                deletePlaceUseCase, findPlaceByIdUseCase, findAllPlacesUseCase,
                findLinkedEntitiesUseCase);
    }

    @Test
    void create_shouldReturn201AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(createPlaceUseCase.create(any())).thenReturn(buildPlace(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreatePlaceRequest("Thornvale", "A city", "Body",
                List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null, null,"en", null, null, null, false, null, null, false);

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
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreatePlaceRequest("Thornvale", null, null,
                List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null, null,"en", null, null, null, false, null, null, false);

        controller.create(request);

        ArgumentCaptor<CreatePlaceUseCase.Command> captor =
                ArgumentCaptor.forClass(CreatePlaceUseCase.Command.class);
        verify(createPlaceUseCase).create(captor.capture());
        assertThat(captor.getValue().mapType()).isEqualTo(MapType.NAVIGABLE);
    }

    @Test
    void create_shouldDefaultToDraftStatus_whenStatusIsNull() {
        UUID id = UUID.randomUUID();
        when(createPlaceUseCase.create(any())).thenReturn(buildPlace(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreatePlaceRequest("Thornvale", null, null,
                List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null, null,"en", null, null, null, false, null, null, false);

        controller.create(request);

        ArgumentCaptor<CreatePlaceUseCase.Command> captor =
                ArgumentCaptor.forClass(CreatePlaceUseCase.Command.class);
        verify(createPlaceUseCase).create(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void create_shouldPassCanonStatus_whenStatusIsCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Place canonPlace = new Place(id, "Thornvale", null, null, List.of(),
                List.of(PlaceCategory.CITY), MapType.NAVIGABLE, EntityStatus.CANON, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(createPlaceUseCase.create(any())).thenReturn(canonPlace);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreatePlaceRequest("Thornvale", null, null,
                List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null, "CANON","en", null, null, null, false, null, null, false);

        controller.create(request);

        ArgumentCaptor<CreatePlaceUseCase.Command> captor =
                ArgumentCaptor.forClass(CreatePlaceUseCase.Command.class);
        verify(createPlaceUseCase).create(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void create_shouldThrowIllegalArgumentException_whenStatusIsDeprecated() {
        var request = new CreatePlaceRequest("Thornvale", null, null,
                List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null, "DEPRECATED","en", null, null, null, false, null, null, false);

        assertThatThrownBy(() -> controller.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DEPRECATED");
    }

    @Test
    void update_shouldReturn200AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(updatePlaceUseCase.update(eq(id), any())).thenReturn(buildPlace(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new UpdatePlaceRequest("Thornvale Updated", null, null,
                List.of(), List.of("CITY"), "NAVIGABLE", "era-1", null, null, false, null, null, false);

        var response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(updatePlaceUseCase).update(eq(id), any());
    }

    @Test
    void changeStatus_shouldReturn200AndCallUseCase() {
        UUID id = UUID.randomUUID();
        when(changePlaceStatusUseCase.changeStatus(id, EntityStatus.CANON)).thenReturn(buildPlace(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

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
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findAll("en", null, null, 0, 20);

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

        controller.findAll("en", null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllPlacesUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).isEmpty();
    }

    @Test
    void findById_shouldDelegateAndMapResult() {
        UUID id = UUID.randomUUID();
        when(findPlaceByIdUseCase.findById(id)).thenReturn(buildPlace(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findPlaceByIdUseCase).findById(id);
    }
}
