package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.faction.CreateFactionUseCase;
import com.keynor.core.domain.port.in.faction.UpdateFactionUseCase;
import com.keynor.core.domain.port.out.FactionRepository;
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
class FactionServiceTest {

    @Mock
    private FactionRepository factionRepository;

    private FactionService factionService;

    @BeforeEach
    void setUp() {
        factionService = new FactionService(factionRepository);
    }

    @Test
    void create_shouldReturnSavedFaction_whenNameIsUnique() {
        var command = new CreateFactionUseCase.Command(
                "The Silver Order", "A guild of mages", "Long description...",
                List.of("mages", "arcane"),
                List.of(),
                List.of(FactionCategory.ORDER),
                null);
        when(factionRepository.existsByName("The Silver Order")).thenReturn(false);
        when(factionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Faction result = factionService.create(command);

        assertThat(result.getName()).isEqualTo("The Silver Order");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(result.getCategories()).containsExactly(FactionCategory.ORDER);
        verify(factionRepository).save(any());
    }

    @Test
    void create_shouldThrowDuplicateEntityNameException_whenNameAlreadyExists() {
        var command = new CreateFactionUseCase.Command(
                "The Silver Order", null, null, List.of(), List.of(),
                List.of(FactionCategory.GUILD), null);
        when(factionRepository.existsByName("The Silver Order")).thenReturn(true);

        assertThatThrownBy(() -> factionService.create(command))
                .isInstanceOf(DuplicateEntityNameException.class)
                .hasMessageContaining("The Silver Order");
        verify(factionRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnFaction_whenFactionExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Faction faction = new Faction(id, "The Silver Order", null, null, List.of(), List.of(),
                List.of(FactionCategory.ORDER), EntityStatus.DRAFT, null, now, now);
        when(factionRepository.findById(id)).thenReturn(Optional.of(faction));

        Faction result = factionService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenFactionDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(factionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> factionService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        EntityFilter filter = EntityFilter.empty();
        PageRequest pageRequest = new PageRequest(0, 10);
        when(factionRepository.findAll(filter, pageRequest))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        PageResult<Faction> result = factionService.findAll(filter, pageRequest);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void changeStatus_shouldTransitionFromDraftToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Faction faction = new Faction(id, "The Silver Order", null, null, List.of(), List.of(),
                List.of(FactionCategory.ORDER), EntityStatus.DRAFT, null, now, now);
        when(factionRepository.findById(id)).thenReturn(Optional.of(faction));
        when(factionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Faction result = factionService.changeStatus(id, EntityStatus.CANON);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void changeStatus_shouldThrowInvalidStatusTransitionException_whenDeprecatedToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Faction faction = new Faction(id, "The Silver Order", null, null, List.of(), List.of(),
                List.of(FactionCategory.ORDER), EntityStatus.DEPRECATED, null, now, now);
        when(factionRepository.findById(id)).thenReturn(Optional.of(faction));

        assertThatThrownBy(() -> factionService.changeStatus(id, EntityStatus.CANON))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void delete_shouldCallRepository_whenFactionExists() {
        UUID id = UUID.randomUUID();
        when(factionRepository.existsById(id)).thenReturn(true);

        factionService.delete(id);

        verify(factionRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenFactionDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(factionRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> factionService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldReplaceFields_whenFactionExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Faction faction = new Faction(id, "Old Name", null, null, List.of(), List.of(),
                List.of(FactionCategory.GUILD), EntityStatus.DRAFT, null, now, now);
        when(factionRepository.findById(id)).thenReturn(Optional.of(faction));
        when(factionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateFactionUseCase.Command(
                "New Name", "New summary", "New body",
                List.of("tag1"), List.of(),
                List.of(FactionCategory.EMPIRE, FactionCategory.ORDER), null);

        Faction result = factionService.update(id, command);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCategories()).containsExactlyInAnyOrder(FactionCategory.EMPIRE, FactionCategory.ORDER);
    }
}
