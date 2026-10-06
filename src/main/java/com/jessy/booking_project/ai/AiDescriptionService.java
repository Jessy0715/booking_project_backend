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
 * <p>照片怎麼交給 Claude 依來源而定，見 {@link #imageSourceOf(String)}：
 * 我們自己存的讀成 base64（本機模式的網址是 localhost，Anthropic 連不到），
 * 外部網址則直接交給 Anthropic 去抓。
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
        // 防呆的第一層在 DTO 的 @NotBlank（少一樣直接 400，不會走到這裡）
        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                // 60 字的中文大約 100 個 token，給 300 是留餘裕，不是預期會用到
                .maxTokens(300L)
                .system(SYSTEM_PROMPT)
                .addUserMessageOfBlockParams(List.of(
                        // 圖片放文字前面：官方建議的順序，模型對圖的理解較好
                        ContentBlockParam.ofImage(
                                ImageBlockParam.builder().source(imageSourceOf(request.roomImg())).build()),
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
     * 決定怎麼把圖片交給 Claude，依網址是不是我們存的分兩條路。
     *
     * <table>
     *   <tr><th>網址來源</th><th>做法</th><th>誰去抓圖</th></tr>
     *   <tr><td>我們存的（Cloudinary / 本機）</td><td>讀 bytes 轉 base64</td><td>我們的後端</td></tr>
     *   <tr><td>外部的（種子資料的 Unsplash 等）</td><td>直接給網址</td><td>Anthropic 的伺服器</td></tr>
     * </table>
     *
     * <p><b>為什麼外部網址不自己抓</b>：那會變成 SSRF 漏洞 —— 前端若送
     * {@code http://localhost:8088/...} 或雲端的 metadata 端點，後端就成了攻擊者的跳板。
     * 交給 Anthropic 去抓，我們一行都不碰那個網址，攻擊者填內部位址也沒用（對方連不到）。
     *
     * <p>附帶好處：外部網址常常沒有副檔名（Unsplash 就是），交給 Anthropic 就不用自己猜
     * media type，它看 HTTP 的 Content-Type。
     */
    private ImageBlockParam.Source imageSourceOf(String url) {
        if (!imageStorage.owns(url)) {
            return ImageBlockParam.Source.ofUrl(url);
        }

        // 自己存的才讀 bytes。本機模式的網址是 localhost，Anthropic 連不到，只能走 base64
        byte[] image = imageStorage.read(url);
        return ImageBlockParam.Source.ofBase64(Base64ImageSource.builder()
                .data(Base64.getEncoder().encodeToString(image))
                .mediaType(mediaTypeOf(url))
                .build());
    }

    /**
     * 從副檔名判斷 media type。
     *
     * <p>只有「我們自己存的網址」會走到這裡，而那些檔案在上傳時已經驗過 magic bytes
     * （見 UploadService），所以這裡信任副檔名是安全的。
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
