package com.jessy.booking_project.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlockParam;
import com.jessy.booking_project.ai.dto.RoomDescriptionRequest;
import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import com.jessy.booking_project.upload.ImageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

/**
 * 依場地名稱與照片，用 Claude 生成一段場地說明。
 *
 * <p>照片不是把網址丟給 Claude，而是後端先讀出 bytes 轉 base64 ——
 * 本機開發的網址是 localhost，Anthropic 的伺服器連不到。
 * 改成 Cloudinary 後雖然網址公開可讀，但用同一套做法比較單純。
 */
@Slf4j
@Service
public class AiDescriptionService {

    /**
     * 系統提示。三件事最重要：
     * <ol>
     *   <li><b>禁止臆造</b> —— 不寫就會編出照片裡沒有的設備、坪數、價格</li>
     *   <li><b>禁止行銷腔</b> —— 不寫就會出現「絕美」「頂級」「打造夢想」</li>
     *   <li><b>只輸出結果</b> —— 不寫就會回「好的，以下是場地說明：」</li>
     * </ol>
     */
    private static final String SYSTEM_PROMPT = """
            你是攝影棚租借平台的文案編輯。根據使用者提供的場地名稱與照片，寫一段場地說明。

            規則：
            - 使用繁體中文，60 字以內
            - 只描述照片裡看得到的、以及場地名稱本身透露的資訊
            - 絕對不要臆造照片中沒有的設備、坪數、樓層、容納人數或價格
            - 語氣平實專業，不要使用行銷詞彙（例如「絕美」「頂級」「打造夢想」）
            - 直接輸出說明文字本身，不要加前言、結語、引號或任何標記
            """;

    private final AnthropicClient claude;
    private final ImageStorage imageStorage;
    private final String model;

    public AiDescriptionService(AnthropicClient claude,
                                ImageStorage imageStorage,
                                @Value("${app.ai.model}") String model) {
        this.claude = claude;
        this.imageStorage = imageStorage;
        this.model = model;
    }

    public String generate(RoomDescriptionRequest request) {
        // 防呆的第一層在 DTO 的 @NotBlank（少一樣直接 400，不會走到這裡）；
        // 第二層是這裡：圖片讀不出來就別花錢呼叫 API
        byte[] image = imageStorage.read(request.roomImg());
        Base64ImageSource.MediaType mediaType = mediaTypeOf(request.roomImg());

        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                // 60 字的中文大約 100 個 token，給 300 是留餘裕，不是預期會用到
                .maxTokens(300L)
                .system(SYSTEM_PROMPT)
                .addUserMessageOfBlockParams(List.of(
                        // 圖片放文字前面：官方建議的順序，模型對圖的理解較好
                        ContentBlockParam.ofImage(ImageBlockParam.builder()
                                .source(Base64ImageSource.builder()
                                        .data(Base64.getEncoder().encodeToString(image))
                                        .mediaType(mediaType)
                                        .build())
                                .build()),
                        ContentBlockParam.ofText(TextBlockParam.builder()
                                .text("場地名稱：" + request.title())
                                .build())))
                .build();

        long startedAt = System.currentTimeMillis();
        try {
            Message response = claude.messages().create(params);
            String description = extractText(response);

            // 這是會花錢的操作：記下 token 用量才能對帳、才能發現異常暴增。
            // 不記生成出來的文字本身 —— 那是使用者內容，log 不是存放它的地方
            log.info("AI 生成場地說明：model={}, inputTokens={}, outputTokens={}, 耗時={}ms, 長度={}字",
                    model,
                    response.usage().inputTokens(),
                    response.usage().outputTokens(),
                    System.currentTimeMillis() - startedAt,
                    description.length());

            return description;
        } catch (BusinessException e) {
            // extractText 丟的，已經記過 log，直接往上拋不要重複記
            throw e;
        } catch (RuntimeException e) {
            // 對外只回「請稍後再試」，細節寫進 log —— 不把第三方的錯誤訊息透給前端
            log.error("Claude 生成場地說明失敗", e);
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED);
        }
    }

    /** 回應可能被切成多個 text block，全部串起來再去頭尾空白。 */
    private String extractText(Message response) {
        String text = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .reduce("", String::concat)
                .trim();

        if (text.isBlank()) {
            log.error("Claude 回應沒有文字內容：stopReason={}", response.stopReason());
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED);
        }
        return text;
    }

    /**
     * 從副檔名判斷 media type。
     *
     * <p>這裡可以信任副檔名：這些網址都是我們自己的上傳流程產生的，
     * 而那個流程已經驗過 magic bytes 了（見 UploadService）。
     */
    private Base64ImageSource.MediaType mediaTypeOf(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".png")) {
            return Base64ImageSource.MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".webp")) {
            return Base64ImageSource.MediaType.IMAGE_WEBP;
        }
        // 縮圖之後絕大多數都是 jpg，當預設
        return Base64ImageSource.MediaType.IMAGE_JPEG;
    }
}
