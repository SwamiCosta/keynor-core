package com.keynor.core.domain.port.in.place;

import com.keynor.core.domain.model.place.Place;

import java.util.UUID;

public interface FindPlaceByIdUseCase {
    Place findById(UUID id);
}
