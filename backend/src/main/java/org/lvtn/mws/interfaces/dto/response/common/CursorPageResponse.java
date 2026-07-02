package org.lvtn.mws.interfaces.dto.response.common;

import org.lvtn.mws.domain.common.CursorPage;

import java.util.List;
import java.util.function.Function;

/** DTO trả về cho phân trang kiểu con trỏ (keyset). */
public record CursorPageResponse<T>(List<T> content, String nextCursor, boolean hasNext) {

    public static <E, D> CursorPageResponse<D> from(CursorPage<E> page, Function<? super E, ? extends D> mapper) {
        List<D> mapped = page.content().stream().<D>map(mapper).toList();
        return new CursorPageResponse<>(mapped, page.nextCursor(), page.hasNext());
    }
}
