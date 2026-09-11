package com.jessy.booking_project.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 把錯誤直接寫進 response，形狀跟 ApiResponse.fail 一致。
 *
 * <p>Filter 層沒有 Spring MVC 的訊息轉換器可用，所以自己寫。
 * 訊息是程式內定的固定字串，不含使用者輸入，直接串接沒有注入風險。
 */
final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    static void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}
