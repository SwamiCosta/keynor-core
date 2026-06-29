package com.keynor.core.domain.port.in.event;

import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;

public interface CreateEventUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<EventCategory> categories,
            Timeline timeline,
            EntityStatus status,
            List<EntityLinkRef> links) {}

    Event create(Command command);
}
