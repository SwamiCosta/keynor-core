package com.keynor.core.domain.port.in.map;

import com.keynor.core.domain.model.map.GameMap;

import java.util.List;

public interface FindAllMapsUseCase {
    List<GameMap> findAll();
}
