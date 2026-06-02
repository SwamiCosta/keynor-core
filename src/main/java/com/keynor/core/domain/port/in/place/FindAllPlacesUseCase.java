package com.keynor.core.domain.port.in.place;

import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

public interface FindAllPlacesUseCase {
    PageResult<Place> findAll(EntityFilter filter, PageRequest pageRequest);
}
