package com.jessy.booking_project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 全站統一的回應外殼。契約 docs/api-contract.md §2。
 *
 * <p>NON_NULL：值是 null 的欄位不會出現在 JSON 裡，所以同一個 record 能生出三種形狀。
 * <p>component 的宣告順序 = JSON key 的順序。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        PaginationResponse pagination,
        String message
) {

    /** {"success":true,"data":{...}} */
    public static <T> ApiResponse<T> ok(T data) {

        return new ApiResponse<>(true, data, null, null);
    }

    /** {"success":true,"data":[...],"pagination":{...}} */
    public static <T> ApiResponse<T> ok(T data, PaginationResponse pagination) {
        return new ApiResponse<>(true, data, pagination, null);
    }

    /** {"success":false,"message":"場地不存在"} */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, null, message);
    }
}
