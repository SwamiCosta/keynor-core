package com.keynor.core.domain.port.in.item;

import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.EntityStatus;

import java.util.UUID;

public interface ChangeItemStatusUseCase {
    Item changeStatus(UUID id, EntityStatus newStatus);
}
