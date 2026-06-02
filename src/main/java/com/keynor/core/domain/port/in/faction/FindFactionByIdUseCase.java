package com.keynor.core.domain.port.in.faction;

import com.keynor.core.domain.model.faction.Faction;

import java.util.UUID;

public interface FindFactionByIdUseCase {
    Faction findById(UUID id);
}
