package com.keynor.core.domain.port.in.lore;

import com.keynor.core.domain.model.lore.Lore;

import java.util.UUID;

public interface FindLoreByIdUseCase {
    Lore findById(UUID id);
}
