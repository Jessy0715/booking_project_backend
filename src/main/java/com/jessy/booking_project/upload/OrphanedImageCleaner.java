package com.jessy.booking_project.upload;

import com.jessy.booking_project.room.ImageOrphanedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 接收「圖片沒人用了」的事件，把檔案刪掉。
 *
 * <p><b>AFTER_COMMIT 是重點</b>：等資料庫交易確定成功才刪檔。
 * <ul>
 *   <li>先刪檔再 commit → 萬一交易回滾，場地還在但圖沒了，變破圖（救不回來）</li>
 *   <li>commit 後才刪檔 → 萬一刪檔失敗，只是多一個沒人用的孤兒檔（每日排程會清）</li>
 * </ul>
 * 兩種失敗都可能發生，但後者的後果小得多。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanedImageCleaner {

    private final ImageStorage imageStorage;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onImageOrphaned(ImageOrphanedEvent event) {
        String url = event.imageUrl();

        // 種子資料的 Unsplash 網址不是我們存的，owns() 會擋掉
        if (!imageStorage.owns(url)) {
            log.debug("非本站儲存的圖片，略過刪除：{}", url);
            return;
        }

        imageStorage.delete(url);
    }
}
