package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.out.CharacterRepository;
import com.keynor.core.domain.port.out.EraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CharacterJpaAdapterIntegrationTest {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private EraRepository eraRepository;

    private String eraName;

    @BeforeEach
    void seedEra() {
        Instant now = Instant.now();
        Era era = eraRepository.save(new Era(UUID.randomUUID(), "Test Era", 0, EraType.ERA, null, null, now, now, Language.EN, UUID.randomUUID()));
        eraName = era.getName();
    }

    private Character buildCharacter(String name, EntityStatus status, CharacterCategory category) {
        return buildCharacter(name, status, category, false);
    }

    private Character buildCharacter(String name, EntityStatus status, CharacterCategory category, boolean common) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        return new Character(id, name, "Summary of " + name, "Body text",
                List.of(),
                List.of(category), status, new Timeline(eraName, null), now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, common);
    }

    @Test
    void findById_shouldReturnCharacter_whenCharacterExists() {
        Character character = buildCharacter("Araveth", EntityStatus.DRAFT, CharacterCategory.HERO);
        characterRepository.save(character);

        Optional<Character> result = characterRepository.findById(character.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Araveth");
    }

    @Test
    void findById_shouldReturnEmpty_whenCharacterDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<Character> result = characterRepository.findById(nonExistentId);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnOnlyMatchingStatus_whenStatusFilterApplied() {
        characterRepository.save(buildCharacter("Araveth", EntityStatus.DRAFT, CharacterCategory.HERO));
        characterRepository.save(buildCharacter("Morken", EntityStatus.CANON, CharacterCategory.VILLAIN));
        EntityFilter canonFilter = new EntityFilter(Language.EN, List.of(EntityStatus.CANON), List.of(), false, false);

        PageResult<Character> result = characterRepository.findAll(canonFilter, new PageRequest(0, 10));

        assertThat(result.content()).allMatch(c -> c.getStatus() == EntityStatus.CANON);
    }

    @Test
    void findAll_shouldExcludeCommonEntities_whenExcludeCommonIsTrue() {
        characterRepository.save(buildCharacter("Ordinary Villager", EntityStatus.CANON, CharacterCategory.NPC, true));
        characterRepository.save(buildCharacter("Araveth", EntityStatus.CANON, CharacterCategory.HERO, false));
        EntityFilter excludeCommonFilter = new EntityFilter(Language.EN, List.of(EntityStatus.CANON), List.of(), false, true);

        PageResult<Character> result = characterRepository.findAll(excludeCommonFilter, new PageRequest(0, 10));

        assertThat(result.content()).extracting(Character::getName).doesNotContain("Ordinary Villager");
        assertThat(result.content()).extracting(Character::getName).contains("Araveth");
    }

    @Test
    void findAll_shouldIncludeCommonEntities_whenExcludeCommonIsFalse() {
        characterRepository.save(buildCharacter("Ordinary Villager", EntityStatus.CANON, CharacterCategory.NPC, true));
        EntityFilter includeCommonFilter = new EntityFilter(Language.EN, List.of(EntityStatus.CANON), List.of(), false, false);

        PageResult<Character> result = characterRepository.findAll(includeCommonFilter, new PageRequest(0, 10));

        assertThat(result.content()).extracting(Character::getName).contains("Ordinary Villager");
    }

    @Test
    void findAll_shouldReturnPaginatedResults_whenPageSizeLimited() {
        for (int i = 1; i <= 5; i++) {
            characterRepository.save(buildCharacter("Character " + i, EntityStatus.DRAFT, CharacterCategory.NPC));
        }
        EntityFilter emptyFilter = new EntityFilter(Language.EN, List.of(), List.of(), false, false);

        PageResult<Character> firstPage = characterRepository.findAll(emptyFilter, new PageRequest(0, 2));
        PageResult<Character> secondPage = characterRepository.findAll(emptyFilter, new PageRequest(1, 2));

        assertThat(firstPage.content()).hasSize(2);
        assertThat(secondPage.content()).hasSize(2);
        assertThat(firstPage.totalElements()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void existsByName_shouldReturnTrue_whenNameAlreadyExists() {
        characterRepository.save(buildCharacter("UniqueHero", EntityStatus.DRAFT, CharacterCategory.HERO));

        boolean exists = characterRepository.existsByNameAndLanguage("UniqueHero", Language.EN);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_shouldReturnFalse_whenNameDoesNotExist() {
        boolean exists = characterRepository.existsByNameAndLanguage("NeverCreatedCharacter", Language.EN);

        assertThat(exists).isFalse();
    }

    @Test
    void deleteById_shouldRemoveCharacter_whenCharacterExists() {
        Character character = buildCharacter("ToBeDeleted", EntityStatus.DRAFT, CharacterCategory.NPC);
        characterRepository.save(character);
        UUID id = character.getId();

        characterRepository.deleteById(id);

        assertThat(characterRepository.findById(id)).isEmpty();
    }
}
