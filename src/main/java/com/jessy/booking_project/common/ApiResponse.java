package com.jessy.booking_project.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 全站統一的回應外殼。契約 docs/api-contract.md §2。
 *
 * <p>NON_NULL：值是 null 的欄位不會出現在 JSON 裡，所以同一個 record 能生出三種形狀。
 * <p>component 的宣告順序 = JSON key 的順序。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "ApiResponse",
        description = "全站統一的回應外殼。值為 null 的欄位不會出現在 JSON 裡。")
public record ApiResponse<T>(

        @Schema(description = "成功與否。前端的第一個判斷點。", example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        boolean success,

        @Schema(description = "成功時的資料本體。失敗時、或成功但無資料可回時不會出現。")
        T data,

        @Schema(description = "分頁資訊。只有列表型端點會帶。")
        PaginationResponse pagination,

        @Schema(description = "中文訊息，可直接顯示給使用者。失敗時一定有。",
                example = "場地不存在")
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

    /** {"success":true,"message":"場地已刪除"} —— 成功但沒有資料可回。 */
    public static ApiResponse<Void> okMessage(String message) {
        return new ApiResponse<>(true, null, null, message);
    }
}
