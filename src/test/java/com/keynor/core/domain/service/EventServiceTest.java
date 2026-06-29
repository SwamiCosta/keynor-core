package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.event.CreateEventUseCase;
import com.keynor.core.domain.port.in.event.UpdateEventUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.EventRepository;
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
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EntityLinkRepository entityLinkRepository;

    @Mock
    private EraRepository eraRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, entityLinkRepository, eraRepository);
    }

    @Test
    void create_shouldReturnSavedEvent_whenNameIsUnique() {
        var command = new CreateEventUseCase.Command(
                "The Battle of Kor", "A decisive battle", "Long description...",
                List.of(),
                List.of(EventCategory.BATTLE),
                null,
                null,
                null);
        when(eventRepository.existsByName("The Battle of Kor")).thenReturn(false);
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.create(command);

        assertThat(result.getName()).isEqualTo("The Battle of Kor");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(result.getCategories()).containsExactly(EventCategory.BATTLE);
        verify(eventRepository).save(any());
    }

    @Test
    void create_shouldReturnSavedEventWithCanonStatus_whenStatusIsCanon() {
        var command = new CreateEventUseCase.Command(
                "The Battle of Kor", "A decisive battle", "Long description...",
                List.of(),
                List.of(EventCategory.BATTLE),
                null,
                EntityStatus.CANON,
                null);
        when(eventRepository.existsByName("The Battle of Kor")).thenReturn(false);
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.create(command);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
        verify(eventRepository).save(any());
    }

    @Test
    void create_shouldThrowDuplicateEntityNameException_whenNameAlreadyExists() {
        var command = new CreateEventUseCase.Command(
                "The Battle of Kor", null, null, List.of(),
                List.of(EventCategory.POLITICAL), null, null, null);
        when(eventRepository.existsByName("The Battle of Kor")).thenReturn(true);

        assertThatThrownBy(() -> eventService.create(command))
                .isInstanceOf(DuplicateEntityNameException.class)
                .hasMessageContaining("The Battle of Kor");
        verify(eventRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowUnknownEraNameException_whenTimelineEraDoesNotExist() {
        var command = new CreateEventUseCase.Command(
                "The Battle of Kor", null, null, List.of(), List.of(EventCategory.BATTLE),
                new Timeline("Nonexistent Era", null), null, null);
        when(eventRepository.existsByName("The Battle of Kor")).thenReturn(false);
        when(eraRepository.findByName("Nonexistent Era")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.create(command))
                .isInstanceOf(UnknownEraNameException.class)
                .hasMessageContaining("Nonexistent Era");
        verify(eventRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnEvent_whenEventExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Event event = new Event(id, "The Battle of Kor", null, null, List.of(),
                List.of(EventCategory.BATTLE), EntityStatus.DRAFT, null, now, now);
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        Event result = eventService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenEventDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        EntityFilter filter = EntityFilter.empty();
        PageRequest pageRequest = new PageRequest(0, 10);
        when(eventRepository.findAll(filter, pageRequest))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        PageResult<Event> result = eventService.findAll(filter, pageRequest);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void changeStatus_shouldTransitionFromDraftToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Event event = new Event(id, "The Battle of Kor", null, null, List.of(),
                List.of(EventCategory.BATTLE), EntityStatus.DRAFT, null, now, now);
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.changeStatus(id, EntityStatus.CANON);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void changeStatus_shouldThrowInvalidStatusTransitionException_whenDeprecatedToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Event event = new Event(id, "The Battle of Kor", null, null, List.of(),
                List.of(EventCategory.BATTLE), EntityStatus.DEPRECATED, null, now, now);
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.changeStatus(id, EntityStatus.CANON))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void delete_shouldCallRepository_whenEventExists() {
        UUID id = UUID.randomUUID();
        when(eventRepository.existsById(id)).thenReturn(true);

        eventService.delete(id);

        verify(eventRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenEventDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(eventRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> eventService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldReplaceFields_whenEventExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Event event = new Event(id, "Old Name", null, null, List.of(),
                List.of(EventCategory.SOCIAL), EntityStatus.DRAFT, null, now, now);
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateEventUseCase.Command(
                "New Name", "New summary", "New body",
                List.of(),
                List.of(EventCategory.BATTLE, EventCategory.DIVINE), null, null);

        Event result = eventService.update(id, command);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCategories()).containsExactlyInAnyOrder(EventCategory.BATTLE, EventCategory.DIVINE);
    }
}
