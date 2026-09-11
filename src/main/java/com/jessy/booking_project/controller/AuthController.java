package com.jessy.booking_project.controller;

import com.jessy.booking_project.dto.request.LoginRequest;
import com.jessy.booking_project.dto.request.RegisterRequest;
import com.jessy.booking_project.dto.response.ApiResponse;
import com.jessy.booking_project.dto.response.AuthResponse;
import com.jessy.booking_project.dto.response.UserResponse;
import com.jessy.booking_project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 註冊與登入端點。7b 會加 /login。 */
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
}
