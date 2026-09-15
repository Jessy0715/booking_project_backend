package com.jessy.booking_project.demo;

import com.jessy.booking_project.auth.AuthService;
import com.jessy.booking_project.booking.BookingService;
import com.jessy.booking_project.room.RoomService;
import com.jessy.booking_project.upload.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Demo 站的資料重置：把系統恢復成「剛部署好」的狀態。
 *
 * <p>只在 demo profile 啟用（{@code SPRING_PROFILES_ACTIVE=demo}）——
 * 本機開發不會有任何一行被執行，不會誤刪你的資料。
 *
 * <p>順序是刻意的：先刪預約再刪場地（預約有 room_id 外鍵），最後才清圖。
 * 圖放最後是因為它是外部服務，失敗也不該影響資料庫的一致性。
 *
 * <p>每個步驟各自呼叫對應模組的 Service，不碰別人的 Repository —— 專案的跨模組規則。
 */
@Slf4j
@Service
@Profile("demo")
@RequiredArgsConstructor
public class DemoResetService {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final AuthService authService;
    private final UploadService uploadService;

    /**
     * 執行重置。
     *
     * <p>沒有掛整包的 @Transactional：四個步驟各自是獨立交易。
     * 清圖片本來就不在資料庫交易的範圍內（外部 API 無法回滾），
     * 硬包一個大交易只會製造「以為能回滾其實不能」的錯覺。
     */
    public DemoResetResult reset() {
        log.info("Demo 重置開始");

        int bookings = bookingService.deleteAllForDemoReset();
        int users = authService.deleteNonAdminUsers();
        int rooms = roomService.resetToSeedData();
        int images = uploadService.deleteAllImages();

        DemoResetResult result = new DemoResetResult(bookings, users, rooms, images);
        log.info("Demo 重置完成：{}", result);
        return result;
    }
}
