package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.lore.LoreCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.lore.CreateLoreUseCase;
import com.keynor.core.domain.port.in.lore.UpdateLoreUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.LoreRepository;
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
class LoreServiceTest {

    @Mock
    private LoreRepository loreRepository;

    @Mock
    private EntityLinkRepository entityLinkRepository;

    @Mock
    private EraRepository eraRepository;

    private LoreService loreService;

    @BeforeEach
    void setUp() {
        loreService = new LoreService(loreRepository, entityLinkRepository, eraRepository);
    }

    @Test
    void create_shouldReturnSavedLoreWithDraftStatus_whenStatusIsNull() {
        var command = new CreateLoreUseCase.Command(
                "The Age of Silence", "A period of stillness", "Long description...",
                List.of(),
                List.of(LoreCategory.HISTORY),
                null, null, List.of());
        when(loreRepository.existsByName("The Age of Silence")).thenReturn(false);
        when(loreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lore result = loreService.create(command);

        assertThat(result.getName()).isEqualTo("The Age of Silence");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(result.getCategories()).containsExactly(LoreCategory.HISTORY);
        verify(loreRepository).save(any());
    }

    @Test
    void create_shouldReturnSavedLoreWithCanonStatus_whenStatusIsCanon() {
        var command = new CreateLoreUseCase.Command(
                "The Age of Silence", "A period of stillness", "Long description...",
                List.of(),
                List.of(LoreCategory.HISTORY),
                null, EntityStatus.CANON, List.of());
        when(loreRepository.existsByName("The Age of Silence")).thenReturn(false);
        when(loreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lore result = loreService.create(command);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
        verify(loreRepository).save(any());
    }

    @Test
    void create_shouldThrowDuplicateEntityNameException_whenNameAlreadyExists() {
        var command = new CreateLoreUseCase.Command(
                "The Age of Silence", null, null, List.of(),
                List.of(LoreCategory.MYTH), null, null, List.of());
        when(loreRepository.existsByName("The Age of Silence")).thenReturn(true);

        assertThatThrownBy(() -> loreService.create(command))
                .isInstanceOf(DuplicateEntityNameException.class)
                .hasMessageContaining("The Age of Silence");
        verify(loreRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowUnknownEraNameException_whenTimelineEraDoesNotExist() {
        var command = new CreateLoreUseCase.Command(
                "The Age of Silence", null, null, List.of(), List.of(LoreCategory.HISTORY),
                new Timeline("Nonexistent Era", null), null, List.of());
        when(loreRepository.existsByName("The Age of Silence")).thenReturn(false);
        when(eraRepository.findByName("Nonexistent Era")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loreService.create(command))
                .isInstanceOf(UnknownEraNameException.class)
                .hasMessageContaining("Nonexistent Era");
        verify(loreRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnLore_whenLoreExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Lore lore = new Lore(id, "The Age of Silence", null, null, List.of(),
                List.of(LoreCategory.HISTORY), EntityStatus.DRAFT, null, now, now);
        when(loreRepository.findById(id)).thenReturn(Optional.of(lore));

        Lore result = loreService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenLoreDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(loreRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loreService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        EntityFilter filter = EntityFilter.empty();
        PageRequest pageRequest = new PageRequest(0, 10);
        when(loreRepository.findAll(filter, pageRequest))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        PageResult<Lore> result = loreService.findAll(filter, pageRequest);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void changeStatus_shouldTransitionFromDraftToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Lore lore = new Lore(id, "The Age of Silence", null, null, List.of(),
                List.of(LoreCategory.HISTORY), EntityStatus.DRAFT, null, now, now);
        when(loreRepository.findById(id)).thenReturn(Optional.of(lore));
        when(loreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lore result = loreService.changeStatus(id, EntityStatus.CANON);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void changeStatus_shouldThrowInvalidStatusTransitionException_whenDeprecatedToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Lore lore = new Lore(id, "The Age of Silence", null, null, List.of(),
                List.of(LoreCategory.HISTORY), EntityStatus.DEPRECATED, null, now, now);
        when(loreRepository.findById(id)).thenReturn(Optional.of(lore));

        assertThatThrownBy(() -> loreService.changeStatus(id, EntityStatus.CANON))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void delete_shouldCallRepository_whenLoreExists() {
        UUID id = UUID.randomUUID();
        when(loreRepository.existsById(id)).thenReturn(true);

        loreService.delete(id);

        verify(loreRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenLoreDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(loreRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> loreService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldReplaceFields_whenLoreExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Lore lore = new Lore(id, "Old Name", null, null, List.of(),
                List.of(LoreCategory.MYTH), EntityStatus.DRAFT, null, now, now);
        when(loreRepository.findById(id)).thenReturn(Optional.of(lore));
        when(loreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateLoreUseCase.Command(
                "New Name", "New summary", "New body",
                List.of(),
                List.of(LoreCategory.HISTORY, LoreCategory.PROPHECY), null, List.of());

        Lore result = loreService.update(id, command);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCategories()).containsExactlyInAnyOrder(LoreCategory.HISTORY, LoreCategory.PROPHECY);
    }
}
