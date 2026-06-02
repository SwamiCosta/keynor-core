package com.keynor.core.domain.port.in.character;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.shared.EntityStatus;

import java.util.UUID;

public interface ChangeCharacterStatusUseCase {
    Character changeStatus(UUID id, EntityStatus newStatus);
}
