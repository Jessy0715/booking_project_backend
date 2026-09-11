package com.jessy.booking_project.room;

import com.jessy.booking_project.room.dto.PriceRequest;
import com.jessy.booking_project.room.dto.PriceResponse;
import com.jessy.booking_project.room.dto.RoomCreateRequest;
import com.jessy.booking_project.room.dto.RoomResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /** Request → 新的 Entity。沒帶的欄位補預設值，契約說只有 title 必填。 */
    public Room toEntity(RoomCreateRequest request) {
        Room room = new Room();
        applyRequest(room, request);
        return room;
    }

    /** 把 request 的值寫進一個 Room（新建與更新共用，所以更新時不用重寫一次）。 */
    public void applyRequest(Room room, RoomCreateRequest request) {
        room.setTitle(request.title());
        room.setRoomImg(orEmpty(request.roomImg()));
        room.setDescription(orEmpty(request.desc()));
        room.setFloor(orEmpty(request.floor()));
        room.setArea(orZero(request.area()));
        room.setCapacity(orZero(request.capacity()));
        room.setFacilities(toFacilityString(request.facilities()));

        PriceRequest price = request.price();
        room.setPriceMorning(orZero(price == null ? null : price.morning()));
        room.setPriceAfternoon(orZero(price == null ? null : price.afternoon()));
        room.setPriceNight(orZero(price == null ? null : price.night()));
    }

    /** ["閃燈組", "柔光箱"] → "閃燈組,柔光箱"。toFacilityList 的反向操作。 */
    private String toFacilityString(List<String> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return "";
        }
        return facilities.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    private String orEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private int orZero(Integer value) {
        return value == null ? 0 : value;
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
