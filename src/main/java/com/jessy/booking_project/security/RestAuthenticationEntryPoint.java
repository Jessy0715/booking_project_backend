package com.jessy.booking_project.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 沒有身分卻打了需要登入的端點 → 401。
 *
 * <p>為什麼不寫在 GlobalExceptionHandler：這個例外在 <b>Filter 層</b>丟出，
 * 還沒進到 DispatcherServlet，{@code @RestControllerAdvice} 根本攔不到。
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        JsonErrorWriter.write(response, HttpStatus.UNAUTHORIZED, "請先登入");
    }
}
