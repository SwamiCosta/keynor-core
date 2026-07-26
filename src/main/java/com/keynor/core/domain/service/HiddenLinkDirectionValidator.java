package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.HiddenContentLinkViolationException;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityLinkSummary;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;

import java.util.List;

/**
 * Enforces the one-way linking rule from root ARCHITECTURE.md --
 * "Cross-Project Feature: Hidden Content & Black Pins": a hidden entity may
 * link to a visible one, but a visible entity may never link to hidden
 * content. Called by each entity Service right before
 * EntityLinkRepository.replaceLinks, so the rejection happens before any
 * link is persisted.
 */
public final class HiddenLinkDirectionValidator {

    private HiddenLinkDirectionValidator() {}

    public static void validate(boolean sourceHidden, List<EntityLinkRef> links, UniverseEntityLookupRepository lookup) {
        if (sourceHidden || links == null) {
            return;
        }
        for (EntityLinkRef ref : links) {
            boolean targetHidden = lookup.findSummary(ref.targetType(), ref.targetId())
                    .map(EntityLinkSummary::hidden)
                    .orElse(false);
            if (targetHidden) {
                throw new HiddenContentLinkViolationException(ref.targetType(), ref.targetId());
            }
        }
    }
}
