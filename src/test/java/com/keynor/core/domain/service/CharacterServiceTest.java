package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.character.CreateCharacterUseCase;
import com.keynor.core.domain.port.in.character.UpdateCharacterUseCase;
import com.keynor.core.domain.port.out.CharacterRepository;
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
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    private CharacterService characterService;

    @BeforeEach
    void setUp() {
        characterService = new CharacterService(characterRepository);
    }

    @Test
    void create_shouldReturnSavedCharacter_whenNameIsUnique() {
        var command = new CreateCharacterUseCase.Command(
                "Araveth", "A wandering hero", "Long description...",
                List.of("hero", "warrior"),
                List.of(CharacterCategory.HERO),
                null);
        when(characterRepository.existsByName("Araveth")).thenReturn(false);
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Character result = characterService.create(command);

        assertThat(result.getName()).isEqualTo("Araveth");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(result.getCategories()).containsExactly(CharacterCategory.HERO);
        verify(characterRepository).save(any());
    }

    @Test
    void create_shouldThrowDuplicateEntityNameException_whenNameAlreadyExists() {
        var command = new CreateCharacterUseCase.Command(
                "Araveth", null, null, List.of(), List.of(CharacterCategory.NPC), null);
        when(characterRepository.existsByName("Araveth")).thenReturn(true);

        assertThatThrownBy(() -> characterService.create(command))
                .isInstanceOf(DuplicateEntityNameException.class)
                .hasMessageContaining("Araveth");
        verify(characterRepository, never()).save(any());
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenCharacterDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(characterRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> characterService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void changeStatus_shouldTransitionFromDraftToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Character character = new Character(
                id, "Araveth", null, null, List.of(),
                List.of(CharacterCategory.HERO), EntityStatus.DRAFT, null, now, now);
        when(characterRepository.findById(id)).thenReturn(Optional.of(character));
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Character result = characterService.changeStatus(id, EntityStatus.CANON);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void changeStatus_shouldThrowInvalidStatusTransitionException_whenTransitionIsIllegal() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Character character = new Character(
                id, "Araveth", null, null, List.of(),
                List.of(CharacterCategory.HERO), EntityStatus.DEPRECATED, null, now, now);
        when(characterRepository.findById(id)).thenReturn(Optional.of(character));

        assertThatThrownBy(() -> characterService.changeStatus(id, EntityStatus.CANON))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void delete_shouldCallRepository_whenCharacterExists() {
        UUID id = UUID.randomUUID();
        when(characterRepository.existsById(id)).thenReturn(true);

        characterService.delete(id);

        verify(characterRepository).deleteById(id);
    }

    @Test
    void update_shouldUpdateFields_whenCharacterExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Character character = new Character(
                id, "Old Name", null, null, List.of(),
                List.of(CharacterCategory.NPC), EntityStatus.DRAFT, null, now, now);
        when(characterRepository.findById(id)).thenReturn(Optional.of(character));
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateCharacterUseCase.Command(
                "New Name", "New summary", "New body",
                List.of("tag1"), List.of(CharacterCategory.HERO, CharacterCategory.DEITY), null);

        Character result = characterService.update(id, command);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCategories()).containsExactlyInAnyOrder(CharacterCategory.HERO, CharacterCategory.DEITY);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        EntityFilter filter = EntityFilter.empty();
        PageRequest pageRequest = new PageRequest(0, 10);
        when(characterRepository.findAll(filter, pageRequest))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        PageResult<Character> result = characterService.findAll(filter, pageRequest);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }
}
