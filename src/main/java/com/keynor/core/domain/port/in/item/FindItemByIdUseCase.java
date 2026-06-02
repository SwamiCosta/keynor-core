package com.keynor.core.domain.port.in.item;

import com.keynor.core.domain.model.item.Item;

import java.util.UUID;

public interface FindItemByIdUseCase {
    Item findById(UUID id);
}
