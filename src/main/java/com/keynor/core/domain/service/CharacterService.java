package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.character.*;
import com.keynor.core.domain.port.out.CharacterRepository;

import java.time.Instant;
import java.util.UUID;

public class CharacterService implements
        CreateCharacterUseCase,
        UpdateCharacterUseCase,
        ChangeCharacterStatusUseCase,
        DeleteCharacterUseCase,
        FindCharacterByIdUseCase,
        FindAllCharactersUseCase {

    private final CharacterRepository characterRepository;

    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    @Override
    public Character create(CreateCharacterUseCase.Command command) {
        if (characterRepository.existsByName(command.name())) {
            throw new DuplicateEntityNameException("Character", command.name());
        }
        Instant now = Instant.now();
        Character character = new Character(
                UUID.randomUUID(),
                command.name(),
                command.summary(),
                command.body(),
                command.tags(),
                command.categories(),
                EntityStatus.DRAFT,
                command.timeline(),
                now,
                now);
        return characterRepository.save(character);
    }

    @Override
    public Character update(UUID id, UpdateCharacterUseCase.Command command) {
        Character character = characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Character", id));
        character.update(command.name(), command.summary(), command.body(), command.tags(), command.categories(), command.timeline());
        return characterRepository.save(character);
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
}
