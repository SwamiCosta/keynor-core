package com.keynor.core.domain.port.in.era;

import com.keynor.core.domain.model.era.Era;

import java.util.UUID;

public interface FindEraByIdUseCase {
    Era findById(UUID id);
}
