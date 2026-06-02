package com.keynor.core.domain.port.in.event;

import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

public interface FindAllEventsUseCase {
    PageResult<Event> findAll(EntityFilter filter, PageRequest pageRequest);
}
