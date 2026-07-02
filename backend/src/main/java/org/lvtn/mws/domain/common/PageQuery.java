package org.lvtn.mws.domain.common;

/**
 * Tham số phân trang trung lập với hạ tầng (không phụ thuộc Spring Data), dùng ở
 * tầng domain/application. Tầng infrastructure sẽ dịch sang Pageable của Spring.
 *
 * <p>{@code page} tính từ 0. {@code size} bị kẹp trong [1, 200] để tránh tải nặng.
 */
public record PageQuery(int page, int size) {

    private static final int MAX_SIZE = 200;
    private static final int DEFAULT_SIZE = 20;

    public PageQuery {
        if (page < 0) page = 0;
        if (size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }

    public static PageQuery of(Integer page, Integer size) {
        return new PageQuery(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size);
    }
}
