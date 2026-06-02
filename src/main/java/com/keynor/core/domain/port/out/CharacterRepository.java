package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface CharacterRepository {
    Character save(Character character);
    Optional<Character> findById(UUID id);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    void deleteById(UUID id);
    PageResult<Character> findAll(EntityFilter filter, PageRequest pageRequest);
}
