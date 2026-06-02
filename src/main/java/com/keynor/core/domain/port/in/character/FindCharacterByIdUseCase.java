package com.keynor.core.domain.port.in.character;

import com.keynor.core.domain.model.character.Character;

import java.util.UUID;

public interface FindCharacterByIdUseCase {
    Character findById(UUID id);
}
