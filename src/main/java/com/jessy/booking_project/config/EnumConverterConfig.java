package com.jessy.booking_project.config;

import com.jessy.booking_project.booking.BookingStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * EnumConverterConfig 它是登記給 Spring 用的，Spring 在需要時自己拿出來
 * EnumConverterConfig  ──登記──▶  Spring 的轉換器表
 *                                       │
 * GET /api/bookings?status=pending      │
 *         ↓                             │
 * Spring 看到參數型別是 BookingStatus ───┘ 查表 → 找到你登記的 fromValue → 轉

 * 字串轉型別，前端會拋字串，要轉成後端的enum 寫法，如下; fromValue 是自定義方法
 * 讓 @RequestParam 能直接收小寫的 "pending" 轉成 BookingStatus.PENDING。
 *
 * <p>Spring 預設用 Enum.valueOf，大小寫敏感，"pending" 會失敗。
 * 轉換失敗會丟 MethodArgumentTypeMismatchException → GlobalExceptionHandler 已經接成 400。
 */
@Configuration
public class EnumConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(String.class, BookingStatus.class, BookingStatus::fromValue);
    }
}
