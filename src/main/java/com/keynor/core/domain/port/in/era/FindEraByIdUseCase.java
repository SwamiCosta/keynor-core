package com.keynor.core.domain.port.in.era;

import com.keynor.core.domain.model.era.Era;

public interface FindEraByIdUseCase {
    Era findById(String id);
}
