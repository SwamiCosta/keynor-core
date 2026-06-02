package com.keynor.core.domain.port.in.faction;

import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

public interface FindAllFactionsUseCase {
    PageResult<Faction> findAll(EntityFilter filter, PageRequest pageRequest);
}
