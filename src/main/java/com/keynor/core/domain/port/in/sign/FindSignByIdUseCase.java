package com.keynor.core.domain.port.in.sign;

import com.keynor.core.domain.model.sign.Sign;

import java.util.UUID;

public interface FindSignByIdUseCase {
    Sign findById(UUID id);
}
