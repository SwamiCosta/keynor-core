package com.keynor.core.domain.model.shared;

import java.util.List;

public record EntityFilter(
        Language language,
        List<EntityStatus> statuses,
        List<String> categories,
        boolean excludeHidden,
        boolean excludeCommon
) {

    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    public boolean hasCategoryFilter() {
        return categories != null && !categories.isEmpty();
    }
}
