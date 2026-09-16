package com.jessy.booking_project.demo;

import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * 手動觸發 Demo 重置。給兩種人用：
 * <ul>
 *   <li>外部 cron 服務（例如 cron-job.org）定時打 —— 免費主機會休眠，這也順便叫醒它</li>
 *   <li>你自己想立刻把 Demo 站恢復乾淨時</li>
 * </ul>
 *
 * <p><b>不走 JWT，改用固定密鑰</b>：JWT 一小時過期，外部 cron 設固定 token 撐不了幾天。
 * SecurityConfig 把這支設成 permitAll，由這裡自己驗 X-Reset-Key —— 放行不等於沒保護。
 *
 * <p>只在 demo profile 存在。正式環境若誤設 demo profile，這支會清光資料。
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@Profile("demo")
@Tag(name = "Demo 維運")
public class DemoResetController {

    public static final String HEADER = "X-Reset-Key";

    private final DemoResetService demoResetService;
    private final String resetKey;

    public DemoResetController(DemoResetService demoResetService,
                               @Value("${app.demo.reset-key}") String resetKey) {
        this.demoResetService = demoResetService;
        this.resetKey = resetKey;
        if (resetKey == null || resetKey.isBlank()) {
            log.warn("DEMO_RESET_KEY 未設定，/api/demo/reset 會拒絕所有請求");
        }
    }

    @PostMapping("/reset")
    @Operation(
            summary = "立即重置 Demo 資料（破壞性）",
            description = """
                    把 Demo 站的資料恢復成剛部署好的狀態。**破壞性操作，無法復原。**

                    依序：刪除所有預約 → 刪除非 admin 帳號 → 清空場地並重建 10 間種子場地
                    → 清空所有上傳的圖片。admin 帳號會保留。

                    不走 JWT，改帶 `X-Reset-Key` header（值由環境變數 `DEMO_RESET_KEY` 設定）。
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
                    responseCode = "403",
                    description = "X-Reset-Key 沒帶、不對、或後端沒設定密鑰",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "權限不足" }""")))
    })
    public ApiResponse<DemoResetResult> reset(
            @Parameter(in = ParameterIn.HEADER, description = "重置密鑰", required = true)
            @RequestHeader(value = HEADER, required = false) String providedKey) {

        if (!keyMatches(providedKey)) {
            log.warn("Demo 重置被拒：密鑰不符或未設定");
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return ApiResponse.ok(demoResetService.reset());
    }

    /**
     * 常數時間比對，不用 String.equals。
     *
     * <p>equals 遇到第一個不同的字元就回傳，比對時間會透露「前幾個字元對了」，
     * 理論上可以一個字元一個字元猜出來。isEqual 不管對不對都比完整串。
     * Demo 站被這樣攻擊的機率極低，但這是密鑰比對的標準寫法，順手就用對的。
     */
    private boolean keyMatches(String provided) {
        if (resetKey == null || resetKey.isBlank() || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                resetKey.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }
}
