package com.keynor.core.infrastructure.persistence.lore;

import com.keynor.core.domain.model.lore.LoreCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class LoreSpecifications {

    private LoreSpecifications() {}

    public static Specification<LoreEntity> fromFilter(EntityFilter filter) {
        Specification<LoreEntity> spec = Specification.where(null);

        if (filter.hasStatusFilter()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(filter.statuses()));
        }
        if (filter.hasCategoryFilter()) {
            List<LoreCategory> categories = filter.categories().stream()
                    .map(LoreCategory::valueOf)
                    .toList();
            spec = spec.and((root, query, cb) -> root.join("categories").in(categories));
        }

        return spec;
    }
}
