package org.lvtn.mws.domain.common;

/**
 * Tham số phân trang + sắp xếp, trung lập với hạ tầng (không phụ thuộc Spring Data).
 * {@code page} từ 0; {@code size} kẹp [1,200]. {@code sortBy} là KHOÁ sắp xếp
 * (repo tự map sang cột hợp lệ theo whitelist); {@code sortDir} = ASC/DESC.
 */
public record PageQuery(int page, int size, String sortBy, String sortDir) {

    private static final int MAX_SIZE = 200;
    private static final int DEFAULT_SIZE = 20;

    public PageQuery {
        if (page < 0) page = 0;
        if (size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
        if (sortBy != null && sortBy.isBlank()) sortBy = null;
        if (sortDir != null) {
            String d = sortDir.trim().toUpperCase();
            sortDir = d.equals("ASC") ? "ASC" : "DESC";
        }
    }

    /** Tương thích ngược: không sắp xếp tuỳ biến. */
    public PageQuery(int page, int size) {
        this(page, size, null, null);
    }

    public static PageQuery of(Integer page, Integer size) {
        return new PageQuery(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size, null, null);
    }

    public static PageQuery of(Integer page, Integer size, String sortBy, String sortDir) {
        return new PageQuery(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size, sortBy, sortDir);
    }

    /** Có yêu cầu sắp xếp tuỳ biến hay không. */
    public boolean hasSort() { return sortBy != null; }

    /** true nếu tăng dần (ASC); null/khác coi là giảm dần (DESC). */
    public boolean ascending() { return "ASC".equals(sortDir); }
}
