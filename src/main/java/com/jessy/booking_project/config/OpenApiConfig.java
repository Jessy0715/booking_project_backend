package com.jessy.booking_project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 讓 Swagger UI 右上角出現「Authorize」按鈕，貼上 token 後每個請求自動帶
 * {@code Authorization: Bearer xxx}。
 *
 * <p>只影響文件頁面，不影響 Spring Security 的實際規則。
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("攝影棚租借系統 API")
                        .version("v1"))
                // 宣告一種叫 bearerAuth 的認證方式：HTTP header、Bearer、格式 JWT
                .components(new Components().addSecuritySchemes(SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // 預設所有端點都套用；login / register 沒帶 token 也能打，不影響
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
    }
}
