package com.keynor.core.domain.port.in.place;

import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.EntityStatus;

import java.util.UUID;

public interface ChangePlaceStatusUseCase {
    Place changeStatus(UUID id, EntityStatus newStatus);
}
