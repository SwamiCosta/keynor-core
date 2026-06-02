package com.keynor.core.domain.port.in.item;

import java.util.UUID;

public interface DeleteItemUseCase {
    void delete(UUID id);
}
