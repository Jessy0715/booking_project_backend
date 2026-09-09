package com.jessy.booking_project.config;

import com.jessy.booking_project.entity.Room;
import com.jessy.booking_project.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 種子資料：10 間攝影棚 Studio A ~ J。
 *
 * <p>⚠️ 這份內容是<b>依契約範例推算的</b>，只有 Studio A 逐字對得上舊版。
 * 要跟舊版逐欄位一致的話，把舊專案 {@code server/db.js} 的 {@code allSeedRooms} 換進來。
 *
 * <p>只在資料表是空的時候才塞，所以重開 app 不會長出重複資料。
 * 階段 11 換成 Flyway 之後這個 class 會被淘汰。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) {
        if (roomRepository.count() > 0) {
            return;
        }
        roomRepository.saveAll(seedRooms());
        log.info("種子資料已建立：{} 間場地", roomRepository.count());
    }

    private List<Room> seedRooms() {
        return List.of(
                room("Studio A — 日光大空間", "一樓主棚，採光頂天窗，最多可容納 10 人拍攝團隊。",
                        "1F", 80, 10, "閃燈組,柔光箱,背景架,造型椅", 2000, 3000, 4000,
                        "https://images.unsplash.com/photo-1497366216548-37526070297c"),
                room("Studio B — 純白無縫棚", "無縫白牆與地板，適合商品與人像去背拍攝。",
                        "1F", 60, 8, "無縫背景紙,閃燈組,反光板", 1800, 2600, 3400,
                        "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267"),
                room("Studio C — 黑棚控光室", "全黑吸光牆面，控光精準，適合戲劇感人像。",
                        "B1", 55, 6, "黑旗,聚光燈,煙霧機", 1600, 2400, 3200,
                        "https://images.unsplash.com/photo-1524758631624-e2822e304c36"),
                room("Studio D — 生活感實景棚", "木質地板與窗景，適合家居與生活風格拍攝。",
                        "2F", 70, 8, "實景家具,窗簾組,補光燈", 2200, 3200, 4200,
                        "https://images.unsplash.com/photo-1586023492125-27b2c045efd7"),
                room("Studio E — 訪談收音室", "隔音處理，適合 Podcast 與人物訪談。",
                        "2F", 35, 4, "隔音牆,收音麥克風,主播桌", 1400, 2000, 2600,
                        "https://images.unsplash.com/photo-1590602847861-f357a9332bbc"),
                room("Studio F — 挑高多功能棚", "挑高六米，可搭建大型佈景。",
                        "1F", 120, 20, "電動吊桿,升降台,大型背景架", 3000, 4500, 6000,
                        "https://images.unsplash.com/photo-1505373877841-8d25f7d46678"),
                room("Studio G — 美食料理棚", "附中島流理台與冷藏設備，適合食物攝影。",
                        "1F", 45, 6, "中島流理台,冰箱,餐具組,頂光", 2000, 2800, 3600,
                        "https://images.unsplash.com/photo-1556909212-d5b604d0c90d"),
                room("Studio H — 產品微距棚", "恆定光源與微距台，適合珠寶與小物。",
                        "B1", 30, 4, "微距台,環形燈,柔光箱,轉盤", 1500, 2200, 2900,
                        "https://images.unsplash.com/photo-1542038784456-1ea8e935640e"),
                room("Studio I — 綠幕虛擬棚", "全幅綠幕與均勻打光，適合去背與虛擬製作。",
                        "2F", 65, 10, "綠幕,均勻光組,追蹤標記", 2400, 3400, 4400,
                        "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d"),
                room("Studio J — 頂樓天台棚", "戶外天台，黃昏時段光線極佳。",
                        "RF", 90, 15, "戶外電源,遮陽棚,反光板", 1800, 2800, 5000,
                        "https://images.unsplash.com/photo-1519681393784-d120267933ba")
        );
    }

    private Room room(String title, String description, String floor, int area, int capacity,
                      String facilities, int morning, int afternoon, int night, String roomImg) {
        return Room.builder()
                .title(title)
                .description(description)
                .floor(floor)
                .area(area)
                .capacity(capacity)
                .facilities(facilities)
                .priceMorning(morning)
                .priceAfternoon(afternoon)
                .priceNight(night)
                .roomImg(roomImg)
                .build();
    }
}
