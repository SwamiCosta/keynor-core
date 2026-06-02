package com.keynor.core.domain.port.in.lore;

import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.shared.EntityStatus;

import java.util.UUID;

public interface ChangeLoreStatusUseCase {
    Lore changeStatus(UUID id, EntityStatus newStatus);
}
