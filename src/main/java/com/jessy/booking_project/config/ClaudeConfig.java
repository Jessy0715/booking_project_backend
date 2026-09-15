package com.jessy.booking_project.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Claude API client。
 *
 * <p>建一次共用：client 內部有連線池，每次請求都 new 一個會浪費資源。
 *
 * <p>沒設 API key 就直接啟動失敗 —— 寧願啟動當下發現，不要等使用者按了生成鍵才爆。
 */
@Configuration
public class ClaudeConfig {

    @Bean
    public AnthropicClient anthropicClient(@Value("${app.ai.api-key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("沒有設定 ANTHROPIC_API_KEY 環境變數");
        }
        return AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}
