package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.place.PlaceCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.place.CreatePlaceUseCase;
import com.keynor.core.domain.port.in.place.UpdatePlaceUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private EntityLinkRepository entityLinkRepository;

    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        placeService = new PlaceService(placeRepository, entityLinkRepository);
    }

    @Test
    void create_shouldReturnSavedPlace_whenNameIsUnique() {
        var command = new CreatePlaceUseCase.Command(
                "Thornvale", "A fortified city", "Long description...",
                List.of("city", "fortified"),
                List.of(),
                List.of(PlaceCategory.CITY),
                MapType.NAVIGABLE,
                null,
                null);
        when(placeRepository.existsByName("Thornvale")).thenReturn(false);
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Place result = placeService.create(command);

        assertThat(result.getName()).isEqualTo("Thornvale");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(result.getMapType()).isEqualTo(MapType.NAVIGABLE);
        assertThat(result.getCategories()).containsExactly(PlaceCategory.CITY);
        verify(placeRepository).save(any());
    }

    @Test
    void create_shouldThrowDuplicateEntityNameException_whenNameAlreadyExists() {
        var command = new CreatePlaceUseCase.Command(
                "Thornvale", null, null, List.of(), List.of(),
                List.of(PlaceCategory.REGION), MapType.ABSTRACT, null, null);
        when(placeRepository.existsByName("Thornvale")).thenReturn(true);

        assertThatThrownBy(() -> placeService.create(command))
                .isInstanceOf(DuplicateEntityNameException.class)
                .hasMessageContaining("Thornvale");
        verify(placeRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnPlace_whenPlaceExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Place place = new Place(id, "Thornvale", null, null, List.of(), List.of(),
                List.of(PlaceCategory.CITY), MapType.NAVIGABLE, EntityStatus.DRAFT, null, now, now);
        when(placeRepository.findById(id)).thenReturn(Optional.of(place));

        Place result = placeService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Thornvale");
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenPlaceDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(placeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        EntityFilter filter = EntityFilter.empty();
        PageRequest pageRequest = new PageRequest(0, 10);
        when(placeRepository.findAll(filter, pageRequest))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        PageResult<Place> result = placeService.findAll(filter, pageRequest);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void changeStatus_shouldTransitionFromDraftToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Place place = new Place(id, "Thornvale", null, null, List.of(), List.of(),
                List.of(PlaceCategory.CITY), MapType.NAVIGABLE, EntityStatus.DRAFT, null, now, now);
        when(placeRepository.findById(id)).thenReturn(Optional.of(place));
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Place result = placeService.changeStatus(id, EntityStatus.CANON);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void changeStatus_shouldTransitionFromCanonToDeprecated() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Place place = new Place(id, "Thornvale", null, null, List.of(), List.of(),
                List.of(PlaceCategory.CITY), MapType.NAVIGABLE, EntityStatus.CANON, null, now, now);
        when(placeRepository.findById(id)).thenReturn(Optional.of(place));
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Place result = placeService.changeStatus(id, EntityStatus.DEPRECATED);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.DEPRECATED);
    }

    @Test
    void changeStatus_shouldThrowInvalidStatusTransitionException_whenDeprecatedToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Place place = new Place(id, "Thornvale", null, null, List.of(), List.of(),
                List.of(PlaceCategory.CITY), MapType.NAVIGABLE, EntityStatus.DEPRECATED, null, now, now);
        when(placeRepository.findById(id)).thenReturn(Optional.of(place));

        assertThatThrownBy(() -> placeService.changeStatus(id, EntityStatus.CANON))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void delete_shouldCallRepository_whenPlaceExists() {
        UUID id = UUID.randomUUID();
        when(placeRepository.existsById(id)).thenReturn(true);

        placeService.delete(id);

        verify(placeRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenPlaceDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(placeRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> placeService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldReplaceFields_whenPlaceExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Place place = new Place(id, "Old Name", null, null, List.of(), List.of(),
                List.of(PlaceCategory.CITY), MapType.NAVIGABLE, EntityStatus.DRAFT, null, now, now);
        when(placeRepository.findById(id)).thenReturn(Optional.of(place));
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdatePlaceUseCase.Command(
                "New Name", "New summary", "New body",
                List.of("tag1"), List.of(), List.of(PlaceCategory.REALM), MapType.ABSTRACT, null, null);

        Place result = placeService.update(id, command);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getMapType()).isEqualTo(MapType.ABSTRACT);
        assertThat(result.getCategories()).containsExactly(PlaceCategory.REALM);
    }
}
