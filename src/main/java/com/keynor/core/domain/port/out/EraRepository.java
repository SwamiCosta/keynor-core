package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.era.Era;

import java.util.List;
import java.util.Optional;

public interface EraRepository {
    List<Era> findAll();
    Optional<Era> findById(String id);
}
