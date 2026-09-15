package com.jessy.booking_project.auth;

import com.jessy.booking_project.auth.dto.AuthResponse;
import com.jessy.booking_project.auth.dto.LoginRequest;
import com.jessy.booking_project.auth.dto.RegisterRequest;
import com.jessy.booking_project.auth.dto.UserResponse;
import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.config.OpenApiConfig;
import com.jessy.booking_project.security.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 註冊、登入、登出端點。 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth 認證")
public class AuthController {

    private final AuthService authService;

    /** 登入成功回 200（不是 201，沒有建立任何資源）。 */
    @PostMapping("/login")
    @Operation(
            summary = "登入，取得 token",
            description = """
                    不用登入即可呼叫。成功後存下 `token`，之後請求帶 `Authorization: Bearer <token>`。

                    帳號不存在與密碼錯誤回同一個錯誤，無法分辨（刻意設計）。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "登入成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "id": 1,
                                        "account": "admin",
                                        "role": "admin",
                                        "token": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJib29raW5nLXByb2plY3QifQ.xxxxx"
                                      }
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "帳號或密碼是空的",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "帳號與密碼為必填" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "帳號不存在或密碼錯誤",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "帳號或密碼錯誤" }""")))
    })
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "註冊新帳號",
            description = """
                    不用登入即可呼叫。

                    **註冊不會自動登入**，回應沒有 token，要再呼叫一次登入。

                    建立出來的帳號一律是 `user`，無法透過 API 建立 admin。
                    目前沒有密碼強度限制。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "註冊成功（回應沒有 token）",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": { "id": 3, "account": "jessy", "role": "user" }
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "帳號或密碼是空的",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "帳號與密碼為必填" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "帳號已被使用",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "此帳號已被使用" }""")))
    })
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /**
     * 登出。JWT 是無狀態設計（見 JwtAuthenticationFilter），後端不保存、也不撤銷 token，
     * 所以這支不會讓 token 失效 —— 真正讓使用者「登出」的動作是前端把 token 丟掉。
     * 這支只負責確認身分、留一筆稽核 log；SecurityConfig 已把它設成需要合法 token 才能呼叫。
     */
    @PostMapping("/logout")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "登出",
            description = """
                    需要有效 token。

                    **這支不會讓 token 失效**（無狀態 JWT）。真正登出的動作是前端清掉本地 token，
                    所以這支失敗時前端仍應繼續清除本地資料。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "登出成功（回 message 不回 data）",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true, "message": "已登出" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期")
    })
    public ApiResponse<Void> logout(@AuthenticationPrincipal AuthPrincipal me) {
        authService.logout(me);
        return ApiResponse.okMessage("已登出");
    }
}
