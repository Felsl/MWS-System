package org.lvtn.mws.domain.common;

import java.util.List;

/**
 * Kết quả phân trang kiểu con trỏ (keyset). Dùng cho bảng lớn như thẻ kho:
 * chỉ đi tuần tự next (không nhảy trang N), không dùng OFFSET nên không chậm dần.
 *
 * @param content    dữ liệu trang hiện tại
 * @param nextCursor con trỏ để lấy trang kế; null nếu đã hết
 * @param hasNext    còn dữ liệu phía sau hay không
 */
public record CursorPage<T>(List<T> content, String nextCursor, boolean hasNext) {
}
