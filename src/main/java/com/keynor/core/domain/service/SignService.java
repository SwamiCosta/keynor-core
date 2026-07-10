package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.sign.Sign;
import com.keynor.core.domain.port.in.sign.FindAllSignsUseCase;
import com.keynor.core.domain.port.in.sign.FindSignByIdUseCase;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.port.out.SignRepository;

import java.util.List;
import java.util.UUID;

public class SignService implements FindAllSignsUseCase, FindSignByIdUseCase {

    private final SignRepository signRepository;

    public SignService(SignRepository signRepository) {
        this.signRepository = signRepository;
    }

    @Override
    public List<Sign> findAll(Language language) {
        return signRepository.findAllOrderedBySignOrder(language);
    }

    @Override
    public Sign findById(UUID id) {
        return signRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sign", id));
    }
}
