package com.keynor.core.domain.model.shared;

import java.util.UUID;

public record EntityLinkSummary(EntityType type, UUID id, String name, EntityStatus status, boolean hidden) {
}
