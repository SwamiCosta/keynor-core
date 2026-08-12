package com.keynor.core.domain.port.in.event;

import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;
import java.util.UUID;

public interface UpdateEventUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<EventCategory> categories,
            Timeline timeline,
            List<EntityLinkRef> links,
            boolean hidden,
            String riddleText,
            String password,
            boolean common) {}

    Event update(UUID id, Command command);
}
