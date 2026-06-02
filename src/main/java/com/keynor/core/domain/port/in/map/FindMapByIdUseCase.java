package com.keynor.core.domain.port.in.map;

import com.keynor.core.domain.model.map.GameMap;

public interface FindMapByIdUseCase {
    GameMap findById(String id);
}
