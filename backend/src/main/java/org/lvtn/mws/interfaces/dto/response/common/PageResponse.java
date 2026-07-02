package org.lvtn.mws.interfaces.dto.response.common;

import org.lvtn.mws.domain.common.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * DTO trả về cho client khi phân trang. Không lộ trực tiếp Page/PageImpl của Spring
 * (tránh cảnh báo serialize và giữ hợp đồng API ổn định).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    /** Dựng từ PageResult domain, ánh xạ từng phần tử sang DTO response. */
    public static <E, D> PageResponse<D> from(PageResult<E> result, Function<? super E, ? extends D> mapper) {
        List<D> mapped = result.content().stream().<D>map(mapper).toList();
        return new PageResponse<>(
                mapped,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext());
    }
}
