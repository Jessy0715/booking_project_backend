package com.jessy.booking_project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 設定。
 *
 * <p>這裡只放「整份 API 共通」的說明；各端點的細節寫在各自的 Controller 上。
 *
 * <p>注意：這裡<b>不</b>用 addSecurityItem 全域套用 bearerAuth ——
 * 那會讓公開端點（登入、註冊、看場地）也被標成需要 token，前端會誤會。
 * 需要 token 的端點各自標 {@code @SecurityRequirement(name = "bearerAuth")}。
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME = "bearerAuth";

    private static final String DESCRIPTION = """
            所有回應都包一層外殼，值為 null 的欄位不會出現：

            | 情境 | 形狀 |
            |---|---|
            | 一般成功 | `{"success":true,"data":{...}}` |
            | 列表 | `{"success":true,"data":[...],"pagination":{...}}` |
            | 成功無資料（刪除類） | `{"success":true,"message":"場地已刪除"}` |
            | 失敗 | `{"success":false,"message":"場地不存在"}` |

            `message` 是中文，可直接顯示給使用者。路由不存在時回 404 `找不到此路由`。
            """;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("攝影棚租借系統 API")
                        .version("v1")
                        .description(DESCRIPTION))
                // 宣告一種叫 bearerAuth 的認證方式：HTTP header、Bearer、格式 JWT
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登入後取得的 JWT。貼上時不用加 Bearer 前綴。")));
    }
}
