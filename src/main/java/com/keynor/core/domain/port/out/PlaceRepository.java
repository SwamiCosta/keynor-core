package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository {
    Place save(Place place);
    Optional<Place> findById(UUID id);
    boolean existsById(UUID id);
    boolean existsByNameAndLanguage(String name, Language language);
    void deleteById(UUID id);
    PageResult<Place> findAll(EntityFilter filter, PageRequest pageRequest);
}
