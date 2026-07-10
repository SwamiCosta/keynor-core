package com.keynor.core.domain.port.in.era;

import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.shared.Language;

import java.util.List;

public interface FindAllErasUseCase {
    List<Era> findAll(Language language);
}
