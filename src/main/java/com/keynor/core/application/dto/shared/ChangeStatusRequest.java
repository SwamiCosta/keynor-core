package com.keynor.core.application.dto.shared;

import jakarta.validation.constraints.NotBlank;

public record ChangeStatusRequest(@NotBlank String status) {
}
