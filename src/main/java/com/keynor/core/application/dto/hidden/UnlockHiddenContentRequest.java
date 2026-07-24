package com.keynor.core.application.dto.hidden;

import jakarta.validation.constraints.NotBlank;

public record UnlockHiddenContentRequest(@NotBlank String password) {
}
