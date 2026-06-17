package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.era.Era;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EraRepository {
    List<Era> findAllOrderedByIndex();
    Optional<Era> findById(UUID id);
    Era save(Era era);
}
