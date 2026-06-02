package com.keynor.core.domain.model.shared;

import java.util.List;

public record EntityFilter(
        List<EntityStatus> statuses,
        List<String> categories,
        List<String> tags
) {

    public static EntityFilter empty() {
        return new EntityFilter(List.of(), List.of(), List.of());
    }

    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    public boolean hasCategoryFilter() {
        return categories != null && !categories.isEmpty();
    }

    public boolean hasTagFilter() {
        return tags != null && !tags.isEmpty();
    }
}
