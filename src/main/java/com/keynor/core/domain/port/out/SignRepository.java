package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.sign.Sign;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SignRepository {
    List<Sign> findAllOrderedBySignOrder();
    Optional<Sign> findById(UUID id);
}
