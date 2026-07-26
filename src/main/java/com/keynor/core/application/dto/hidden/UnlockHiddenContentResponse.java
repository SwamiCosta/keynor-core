package com.keynor.core.application.dto.hidden;

import java.time.Instant;

public record UnlockHiddenContentResponse(String token, boolean unlockedAll, Instant expiresAt) {
}
