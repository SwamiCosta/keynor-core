package com.keynor.core.application.dto.shared;

import com.keynor.core.domain.model.shared.PageResult;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements) {

    public static <D, T> PagedResponse<T> from(PageResult<D> result, Function<D, T> mapper) {
        return new PagedResponse<>(
                result.content().stream().map(mapper).toList(),
                result.page(),
                result.size(),
                result.totalElements());
    }
}
