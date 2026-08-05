package com.keynor.core.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Public self-registration (2026-08-05) — always creates a {@code DEFAULT}-role user. There is
 * no way to self-register as {@code ADMIN}; that role is provisioned manually, per the user's
 * explicit decision (see {@code security-model.md}).
 */
public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password) {
}
