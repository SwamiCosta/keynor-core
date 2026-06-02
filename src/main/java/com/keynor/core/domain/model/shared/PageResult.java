package com.keynor.core.domain.model.shared;

import java.util.List;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {
}
