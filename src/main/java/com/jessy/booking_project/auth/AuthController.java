package com.jessy.booking_project.auth;

import com.jessy.booking_project.auth.dto.AuthResponse;
import com.jessy.booking_project.auth.dto.LoginRequest;
import com.jessy.booking_project.auth.dto.RegisterRequest;
import com.jessy.booking_project.auth.dto.UserResponse;
import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.security.AuthPrincipal;
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
public class AuthController {

    private final AuthService authService;

    /** 登入成功回 200（不是 201，沒有建立任何資源）。 */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /**
     * 登出。JWT 是無狀態設計（見 JwtAuthenticationFilter），後端不保存、也不撤銷 token，
     * 所以這支不會讓 token 失效 —— 真正讓使用者「登出」的動作是前端把 token 丟掉。
     * 這支只負責確認身分、留一筆稽核 log；SecurityConfig 已把它設成需要合法 token 才能呼叫。
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal AuthPrincipal me) {
        authService.logout(me);
        return ApiResponse.okMessage("已登出");
    }
}
