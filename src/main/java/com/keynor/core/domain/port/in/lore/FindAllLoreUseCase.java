package com.keynor.core.domain.port.in.lore;

import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

public interface FindAllLoreUseCase {
    PageResult<Lore> findAll(EntityFilter filter, PageRequest pageRequest);
}
