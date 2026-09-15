package com.jessy.booking_project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 把本機的上傳資料夾對應到 /uploads/** 網址，瀏覽器才拿得到圖。
 *
 * <p>只有 LocalImageStorage 需要這個；storage 設成 cloudinary 時圖片由雲端直接提供，這個設定不會載入。
 */
@Configuration
@ConditionalOnProperty(name = "app.upload.storage", havingValue = "local", matchIfMissing = true)
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
