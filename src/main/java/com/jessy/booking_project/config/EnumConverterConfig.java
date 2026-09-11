package com.jessy.booking_project.config;

import com.jessy.booking_project.entity.BookingStatus;
import com.jessy.booking_project.entity.TimeSlot;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
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
        registry.addConverter(String.class, TimeSlot.class, TimeSlot::fromValue);
    }
}
