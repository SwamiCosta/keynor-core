package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.character.CreateCharacterUseCase;
import com.keynor.core.domain.port.in.character.UpdateCharacterUseCase;
import com.keynor.core.domain.port.in.shared.CreateHiddenContentLockUseCase;
import com.keynor.core.domain.port.out.CharacterRepository;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;
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

    @Mock
    private EntityLinkRepository entityLinkRepository;

    @Mock
    private EraRepository eraRepository;

    @Mock
    private UniverseEntityLookupRepository universeEntityLookupRepository;

    @Mock
    private CreateHiddenContentLockUseCase createHiddenContentLockUseCase;

    private CharacterService characterService;

    @BeforeEach
    void setUp() {
        characterService = new CharacterService(characterRepository, entityLinkRepository, eraRepository,
                universeEntityLookupRepository, createHiddenContentLockUseCase);
    }

    @Test
    void create_shouldReturnSavedCharacter_whenNameIsUnique() {
        var command = new CreateCharacterUseCase.Command(
                "Araveth", "A wandering hero", "Long description...",
                List.of(),
                List.of(CharacterCategory.HERO),
                null,
                null, Language.EN, null, null,
                null, false, null, null, false);
        when(characterRepository.existsByNameAndLanguage("Araveth", Language.EN)).thenReturn(false);
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Character result = characterService.create(command);

        assertThat(result.getName()).isEqualTo("Araveth");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(result.getCategories()).containsExactly(CharacterCategory.HERO);
        verify(characterRepository).save(any());
    }

    @Test
    void create_shouldReturnSavedCharacterWithCanonStatus_whenStatusIsCanon() {
        var command = new CreateCharacterUseCase.Command(
                "Araveth", "A wandering hero", "Long description...",
                List.of(),
                List.of(CharacterCategory.HERO),
                null,
                EntityStatus.CANON, Language.EN, null, null,
                null, false, null, null, false);
        when(characterRepository.existsByNameAndLanguage("Araveth", Language.EN)).thenReturn(false);
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Character result = characterService.create(command);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
        verify(characterRepository).save(any());
    }

    @Test
    void create_shouldThrowDuplicateEntityNameException_whenNameAlreadyExists() {
        var command = new CreateCharacterUseCase.Command(
                "Araveth", null, null, List.of(), List.of(CharacterCategory.COMPANION), null, null, Language.EN, null, null, null, false, null, null, false);
        when(characterRepository.existsByNameAndLanguage("Araveth", Language.EN)).thenReturn(true);

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
                List.of(CharacterCategory.HERO), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
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
                List.of(CharacterCategory.HERO), EntityStatus.DEPRECATED, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
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
                List.of(CharacterCategory.COMPANION), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(characterRepository.findById(id)).thenReturn(Optional.of(character));
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateCharacterUseCase.Command(
                "New Name", "New summary", "New body",
                List.of(), List.of(CharacterCategory.HERO, CharacterCategory.DEITY), null, null, false, null, null, false);

        Character result = characterService.update(id, command);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCategories()).containsExactlyInAnyOrder(CharacterCategory.HERO, CharacterCategory.DEITY);
    }

    @Test
    void create_shouldThrowUnknownEraNameException_whenTimelineEraDoesNotExist() {
        var command = new CreateCharacterUseCase.Command(
                "Araveth", null, null, List.of(), List.of(CharacterCategory.HERO),
                new Timeline("Nonexistent Era", null), null, Language.EN, null, null, null, false, null, null, false);
        when(characterRepository.existsByNameAndLanguage("Araveth", Language.EN)).thenReturn(false);
        when(eraRepository.findByName("Nonexistent Era")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> characterService.create(command))
                .isInstanceOf(UnknownEraNameException.class)
                .hasMessageContaining("Nonexistent Era");
        verify(characterRepository, never()).save(any());
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        EntityFilter filter = new EntityFilter(Language.EN, List.of(), List.of(), false, false);
        PageRequest pageRequest = new PageRequest(0, 10);
        when(characterRepository.findAll(filter, pageRequest))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        PageResult<Character> result = characterService.findAll(filter, pageRequest);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void findByIds_shouldDelegateToRepository() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Character character = new Character(id, "Araveth", null, null, List.of(),
                List.of(CharacterCategory.HERO), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(characterRepository.findAllByIds(List.of(id))).thenReturn(List.of(character));

        List<Character> result = characterService.findByIds(List.of(id));

        assertThat(result).containsExactly(character);
    }

    @Test
    void create_shouldReturnCharacterWithImages_whenImagesProvided() {
        var command = new CreateCharacterUseCase.Command(
                "Araveth", null, null,
                List.of("https://example.com/araveth.png", "https://example.com/araveth2.png"),
                List.of(CharacterCategory.HERO), null, null, Language.EN, null, null, null, false, null, null, false);
        when(characterRepository.existsByNameAndLanguage("Araveth", Language.EN)).thenReturn(false);
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Character result = characterService.create(command);

        assertThat(result.getImages()).containsExactly(
                "https://example.com/araveth.png",
                "https://example.com/araveth2.png");
    }

    @Test
    void create_shouldReturnCharacterWithEmptyImages_whenNoImagesProvided() {
        var command = new CreateCharacterUseCase.Command(
                "Araveth", null, null, List.of(), List.of(CharacterCategory.HERO), null, null, Language.EN, null, null, null, false, null, null, false);
        when(characterRepository.existsByNameAndLanguage("Araveth", Language.EN)).thenReturn(false);
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Character result = characterService.create(command);

        assertThat(result.getImages()).isEmpty();
    }

    @Test
    void update_shouldReplaceImagesWithNewList() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Character character = new Character(
                id, "Araveth", null, null,
                List.of("https://example.com/old.png"),
                List.of(CharacterCategory.HERO), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(characterRepository.findById(id)).thenReturn(Optional.of(character));
        when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateCharacterUseCase.Command(
                "Araveth", null, null,
                List.of("https://example.com/new1.png", "https://example.com/new2.png"),
                List.of(CharacterCategory.HERO), null, null, false, null, null, false);

        Character result = characterService.update(id, command);

        assertThat(result.getImages()).containsExactly(
                "https://example.com/new1.png",
                "https://example.com/new2.png");
    }
}
