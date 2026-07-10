package com.keynor.core.infrastructure.persistence.item;

import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ItemSpecifications {

    private ItemSpecifications() {}

    public static Specification<ItemEntity> fromFilter(EntityFilter filter) {
        Specification<ItemEntity> spec = Specification.where(hasLanguage(filter.language()));

        if (filter.hasStatusFilter()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(filter.statuses()));
        }
        if (filter.hasCategoryFilter()) {
            List<ItemCategory> categories = filter.categories().stream()
                    .map(ItemCategory::valueOf)
                    .toList();
            spec = spec.and((root, query, cb) -> root.join("categories").in(categories));
        }

        return spec;
    }

    private static Specification<ItemEntity> hasLanguage(Language language) {
        return (root, query, cb) -> cb.equal(root.get("language"), language);
    }
}
