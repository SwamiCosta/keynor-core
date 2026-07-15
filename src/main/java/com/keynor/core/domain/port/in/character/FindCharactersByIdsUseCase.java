package com.keynor.core.domain.port.in.character;

import com.keynor.core.domain.model.character.Character;

import java.util.List;
import java.util.UUID;

public interface FindCharactersByIdsUseCase {
    List<Character> findByIds(List<UUID> ids);
}
