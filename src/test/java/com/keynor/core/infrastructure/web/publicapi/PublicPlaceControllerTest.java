package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.place.PlaceResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.place.PlaceCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.place.FindAllPlacesUseCase;
import com.keynor.core.domain.port.in.place.FindPlaceByIdUseCase;
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
class PublicPlaceControllerTest {

    @Mock
    private FindAllPlacesUseCase findAllPlacesUseCase;

    @Mock
    private FindPlaceByIdUseCase findPlaceByIdUseCase;

    private PublicPlaceController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicPlaceController(findAllPlacesUseCase, findPlaceByIdUseCase);
    }

    @Test
    void findAll_shouldAlwaysFilterByCanonStatus() {
        when(findAllPlacesUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllPlacesUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).containsExactly(EntityStatus.CANON);
    }

    @Test
    void findAll_shouldPassPaginationParamsToUseCase() {
        when(findAllPlacesUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 2, 50, 0));

        controller.findAll(null, null, 2, 50);

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllPlacesUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(2);
        assertThat(pageCaptor.getValue().size()).isEqualTo(50);
    }

    @Test
    void findAll_shouldReturnMappedPagedResponse() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Place place = new Place(id, "Erevan", "A city", "Body", List.of("city"),
                List.of(), List.of(PlaceCategory.CITY), null, EntityStatus.CANON, null, now, now);
        when(findAllPlacesUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(place), 0, 20, 1));

        var response = controller.findAll(null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<PlaceResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        assertThat(body.content()).hasSize(1);
        assertThat(body.content().get(0).id()).isEqualTo(id);
        assertThat(body.content().get(0).name()).isEqualTo("Erevan");
    }

    @Test
    void findById_shouldDelegateToUseCaseAndMapResult() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Place place = new Place(id, "Erevan", "A city", "Body", List.of(),
                List.of(), List.of(PlaceCategory.REGION), null, EntityStatus.CANON, null, now, now);
        when(findPlaceByIdUseCase.findById(id)).thenReturn(place);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findPlaceByIdUseCase).findById(id);
    }
}
