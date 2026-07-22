package com.keynor.core.domain.port.in.map;

import java.util.UUID;

public interface DeleteMapPinUseCase {
    void delete(String mapId, UUID pinId);
}
