package com.keynor.core.infrastructure.persistence.event;

import com.keynor.core.domain.model.event.EventCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class EventSpecifications {

    private EventSpecifications() {}

    public static Specification<EventEntity> fromFilter(EntityFilter filter) {
        Specification<EventEntity> spec = Specification.where(null);

        if (filter.hasStatusFilter()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(filter.statuses()));
        }
        if (filter.hasCategoryFilter()) {
            List<EventCategory> categories = filter.categories().stream()
                    .map(EventCategory::valueOf)
                    .toList();
            spec = spec.and((root, query, cb) -> root.join("categories").in(categories));
        }
        if (filter.hasTagFilter()) {
            spec = spec.and((root, query, cb) -> root.join("tags").in(filter.tags()));
        }

        return spec;
    }
}
