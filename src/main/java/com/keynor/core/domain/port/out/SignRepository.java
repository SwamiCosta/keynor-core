package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.sign.Sign;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SignRepository {
    List<Sign> findAllOrderedBySignOrder(Language language);
    Optional<Sign> findById(UUID id);
}
