package com.jessy.booking_project.mapper;

import com.jessy.booking_project.dto.response.PriceResponse;
import com.jessy.booking_project.dto.response.RoomResponse;
import com.jessy.booking_project.entity.Room;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/** Entity（資料庫的形狀）→ Response DTO（API 的形狀）。 */
@Component
public class RoomMapper {

    /** 契約的 createdAt 是 "2026-04-16 20:30:00"：空格分隔，不是 ISO。 */
    private static final DateTimeFormatter CREATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    public RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomImg(),
                room.getTitle(),
                room.getDescription(),
                room.getFloor(),
                room.getArea(),
                room.getCapacity(),
                toFacilityList(room.getFacilities()),
                toPrice(room),
                CREATED_AT_FORMAT.format(room.getCreatedAt())
        );
    }

    /** "閃燈組,柔光箱" → ["閃燈組", "柔光箱"]。null 與空字串都回空 List，前端才能安全 .map()。 */
    private List<String> toFacilityList(String facilities) {
        if (facilities == null || facilities.isBlank()) {
            return List.of();
        }
        return Arrays.stream(facilities.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** 三個平坦的 Integer 欄位 → 一個巢狀物件，值轉成契約要的字串。 */
    private PriceResponse toPrice(Room room) {
        return new PriceResponse(
                String.valueOf(room.getPriceMorning()),
                String.valueOf(room.getPriceAfternoon()),
                String.valueOf(room.getPriceNight())
        );
    }
}
