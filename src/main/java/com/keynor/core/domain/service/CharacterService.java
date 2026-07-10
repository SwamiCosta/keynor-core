package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.character.*;
import com.keynor.core.domain.port.out.CharacterRepository;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CharacterService implements
        CreateCharacterUseCase,
        UpdateCharacterUseCase,
        ChangeCharacterStatusUseCase,
        DeleteCharacterUseCase,
        FindCharacterByIdUseCase,
        FindAllCharactersUseCase {

    private final CharacterRepository characterRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final EraRepository eraRepository;

    public CharacterService(
            CharacterRepository characterRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository) {
        this.characterRepository = characterRepository;
        this.entityLinkRepository = entityLinkRepository;
        this.eraRepository = eraRepository;
    }

    @Override
    public Character create(CreateCharacterUseCase.Command command) {
        if (characterRepository.existsByNameAndLanguage(command.name(), command.language())) {
            throw new DuplicateEntityNameException("Character", command.name());
        }
        validateTimeline(command.timeline());
        Instant now = Instant.now();
        EntityStatus initialStatus = command.status() != null ? command.status() : EntityStatus.DRAFT;
        UUID newId = UUID.randomUUID();
        UUID translationGroupId = command.translationGroupId() != null ? command.translationGroupId() : newId;
        Character character = new Character(
                newId,
                command.name(),
                command.summary(),
                command.body(),
                command.images(),
                command.categories(),
                initialStatus,
                command.timeline(),
                now,
                now,
                command.language(),
                translationGroupId);
        Character saved = characterRepository.save(character);
        entityLinkRepository.replaceLinks(EntityType.CHARACTER, saved.getId(), command.links() != null ? command.links() : List.of());
        return saved;
    }

    @Override
    public Character update(UUID id, UpdateCharacterUseCase.Command command) {
        Character character = characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Character", id));
        validateTimeline(command.timeline());
        character.update(command.name(), command.summary(), command.body(), command.images(), command.categories(), command.timeline());
        Character saved = characterRepository.save(character);
        entityLinkRepository.replaceLinks(EntityType.CHARACTER, saved.getId(), command.links() != null ? command.links() : List.of());
        return saved;
    }

    @Override
    public Character changeStatus(UUID id, EntityStatus newStatus) {
        Character character = characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Character", id));
        character.changeStatus(newStatus);
        return characterRepository.save(character);
    }

    @Override
    public void delete(UUID id) {
        if (!characterRepository.existsById(id)) {
            throw new EntityNotFoundException("Character", id);
        }
        characterRepository.deleteById(id);
        entityLinkRepository.deleteAllForEntity(EntityType.CHARACTER, id);
    }

    @Override
    public Character findById(UUID id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Character", id));
    }

    @Override
    public PageResult<Character> findAll(EntityFilter filter, PageRequest pageRequest) {
        return characterRepository.findAll(filter, pageRequest);
    }

    private void validateTimeline(Timeline timeline) {
        if (timeline == null) return;
        validateEraName(timeline.founded());
        validateEraName(timeline.destroyed());
    }

    private void validateEraName(String eraName) {
        if (eraName == null) return;
        if (eraRepository.findByName(eraName).isEmpty()) {
            throw new UnknownEraNameException(eraName);
        }
    }
}
