package com.jessy.booking_project.common;

/** 分頁資訊。契約：{"page":1,"pageSize":5,"total":10,"totalPages":2}，page 從 1 開始。 */
public record PaginationResponse(
        int page,
        int pageSize,
        long total,
        int totalPages) {
}
