package org.lvtn.mws.domain.common;

import java.util.List;
import java.util.function.Function;

/**
 * Kết quả phân trang trung lập với hạ tầng.
 *
 * @param content       dữ liệu trang hiện tại
 * @param page          số trang (từ 0)
 * @param size          kích thước trang yêu cầu
 * @param totalElements tổng số bản ghi khớp điều kiện (bỏ phân trang)
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasNext() {
        return (long) (page + 1) * size < totalElements;
    }

    /** Ánh xạ từng phần tử sang kiểu khác, giữ nguyên metadata phân trang. */
    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        return new PageResult<>(content.stream().<R>map(mapper).toList(),
                page, size, totalElements);
    }
}
