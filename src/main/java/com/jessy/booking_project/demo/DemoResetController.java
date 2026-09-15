package com.jessy.booking_project.demo;

import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 手動觸發 Demo 重置。給兩種人用：
 * <ul>
 *   <li>外部 cron 服務（例如 cron-job.org）定時打 —— 免費主機會休眠，這也順便叫醒它</li>
 *   <li>你自己想立刻把 Demo 站恢復乾淨時</li>
 * </ul>
 *
 * <p>限 admin（SecurityConfig 設定），且只在 demo profile 存在 ——
 * 正式環境若誤設 demo profile，這支會清光資料，所以環境變數要小心。
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@Profile("demo")
@RequiredArgsConstructor
@Tag(name = "Demo 維運")
public class DemoResetController {

    private final DemoResetService demoResetService;

    @PostMapping("/reset")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "立即重置 Demo 資料（admin，破壞性）",
            description = """
                    把 Demo 站的資料恢復成剛部署好的狀態。**破壞性操作，無法復原。**

                    依序：刪除所有預約 → 刪除非 admin 帳號 → 清空場地並重建 10 間種子場地
                    → 清空所有上傳的圖片。admin 帳號會保留。

                    給外部 cron 服務定時呼叫用，一般前端畫面不應該連到這裡。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "重置完成，回報各項處理的數量",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "deletedBookings": 3,
                                        "deletedUsers": 1,
                                        "seededRooms": 10,
                                        "deletedImages": 2
                                      }
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "不是 admin")
    })
    public ApiResponse<DemoResetResult> reset() {

        return ApiResponse.ok(demoResetService.reset());
    }
}
