package com.keynor.core.domain.port.in.event;

import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;

public interface CreateEventUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> tags,
            List<EventCategory> categories,
            Timeline timeline) {}

    Event create(Command command);
}
