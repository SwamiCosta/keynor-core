package com.keynor.core.domain.port.in.sign;

import com.keynor.core.domain.model.sign.Sign;

import java.util.List;

public interface FindAllSignsUseCase {
    List<Sign> findAll();
}
