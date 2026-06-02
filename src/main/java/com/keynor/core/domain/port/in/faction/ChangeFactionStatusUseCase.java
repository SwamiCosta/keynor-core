package com.keynor.core.domain.port.in.faction;

import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.shared.EntityStatus;

import java.util.UUID;

public interface ChangeFactionStatusUseCase {
    Faction changeStatus(UUID id, EntityStatus newStatus);
}
