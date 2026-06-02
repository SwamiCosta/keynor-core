package com.keynor.core.domain.port.in.character;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

public interface FindAllCharactersUseCase {
    PageResult<Character> findAll(EntityFilter filter, PageRequest pageRequest);
}
