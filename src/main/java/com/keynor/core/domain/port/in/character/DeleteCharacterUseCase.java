package com.keynor.core.domain.port.in.character;

import java.util.UUID;

public interface DeleteCharacterUseCase {
    void delete(UUID id);
}
