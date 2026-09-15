package com.jessy.booking_project.room;

import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import com.jessy.booking_project.room.dto.RoomCreateRequest;
import com.jessy.booking_project.room.dto.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** 場地的業務邏輯。進出都是 DTO，Entity 只活在這個 class 內部。 */
@Service
@RequiredArgsConstructor
public class RoomService {

    /** 單頁筆數上限，擋掉 ?pageSize=999999 一次撈全表。 */
    private static final int MAX_PAGE_SIZE = 100;

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    /** 發「圖片沒人用了」的事件。RoomService 不知道圖片存在哪，也不負責刪。 */
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return roomMapper.toResponse(room);
    }

    /**
     * 依關鍵字分頁查詢場地。
     *
     * <p>{@code @Transactional} 把方法內的資料庫操作包成一個交易，要嘛全成功、要嘛全失敗；
     * {@code readOnly = true} 再向 Hibernate 宣告「我只讀不寫」，可跳過髒檢查。
     *
     * @param page 從 1 開始（契約的規則，不是 Spring 的）
     */
    @Transactional(readOnly = true)
    public Page<RoomResponse> search(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(toZeroBased(page), toSafePageSize(pageSize), Sort.by("id"));

        // keyword 沒給時用空字串：LIKE '%%' 匹配全部，省掉 if/else 兩條分支
        Page<Room> rooms = roomRepository.findByTitleContainingIgnoreCase(
                keyword == null ? "" : keyword.trim(), pageable);

        // map 只換掉每一筆的型別，頁碼與總筆數原封不動帶著走
        return rooms.map(roomMapper::toResponse);
    }

    /** 建立場地。@Transactional 不加 readOnly —— 這裡要寫入。 */
    @Transactional
    public RoomResponse create(RoomCreateRequest request) {
        Room room = roomMapper.toEntity(request);
        Room saved = roomRepository.save(room);
        return roomMapper.toResponse(saved);
    }

    @Transactional
    public RoomResponse update(Long id, RoomCreateRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 先記住舊圖網址，等下要比對有沒有被換掉
        String oldImage = room.getRoomImg();

        // 不用呼叫 save()：交易內撈出的 Entity 受 Hibernate 管理，
        // 方法結束時它會自己比對有沒有被改過並發 UPDATE（髒檢查）。
        roomMapper.applyRequest(room, request);

        // 圖真的換了才發事件。沒換圖的編輯（只改價格）不能把圖刪掉
        if (!Objects.equals(oldImage, room.getRoomImg())) {
            publishOrphaned(oldImage);
        }

        return roomMapper.toResponse(room);
    }

    @Transactional
    public void delete(Long id) {
        // 改用 findById：要先拿到 roomImg 才知道等下該刪哪張圖
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        String image = room.getRoomImg();
        roomRepository.delete(room);
        publishOrphaned(image);
    }

    /**
     * 批次刪除。全有全無：只要有一個 id 找不到，整批都不刪。
     *
     * <p>@Transactional 在這裡是真的有用 —— 中途丟例外時，前面已經刪掉的會一起回滾。
     *
     * @return 刪掉幾筆
     */
    @Transactional
    public int deleteAll(List<Long> ids) {
        // 前端可能送出重複的 id（例如清單選了兩次），先去重，數量才會正確
        List<Long> distinctIds = ids.stream().distinct().toList();

        List<Room> rooms = roomRepository.findAllById(distinctIds);

        // 撈到的筆數少於要求的 → 一定有 id 不存在
        if (rooms.size() != distinctIds.size()) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }

        List<String> images = rooms.stream().map(Room::getRoomImg).toList();

        // deleteAllInBatch 發一句 DELETE ... WHERE id IN (...)，
        // 不是每筆各發一句，10 筆就少 9 次來回
        roomRepository.deleteAllInBatch(rooms);

        images.forEach(this::publishOrphaned);
        return rooms.size();
    }

    /**
     * 清空所有場地並重建種子資料。只給 Demo 站每日重置用。
     *
     * <p>不發 ImageOrphanedEvent：重置會把整個圖片儲存空間清空，不需要一張一張刪。
     *
     * @return 重建了幾間場地
     */
    @Transactional
    public int resetToSeedData() {
        roomRepository.deleteAllInBatch();
        return roomRepository.saveAll(RoomSeedData.rooms()).size();
    }

    /**
     * 宣告某張圖沒人用了。事件的接收端設定成「交易 commit 之後」才真的刪檔，
     * 所以這裡即使之後交易回滾，檔案也不會被誤刪。
     */
    private void publishOrphaned(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            events.publishEvent(new ImageOrphanedEvent(imageUrl));
        }
    }


    /**
     * 給其他模組的 Service 用（例如 BookingService）。
     * 回 Entity 是刻意的：Service 之間可以傳 Entity，Controller 不行。
     */
    @Transactional(readOnly = true)
    public Room requireRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    /** 契約的 page 從 1 開始，Spring Data 的 PageRequest 從 0 開始。 */
    private int toZeroBased(int page) {
        return Math.max(page, 1) - 1;
    }

    /** pageSize 夾在 1..MAX_PAGE_SIZE。0 會讓 PageRequest 直接丟例外，太大則是 DoS 風險。 */
    private int toSafePageSize(int pageSize) {
        return Math.clamp(pageSize, 1, MAX_PAGE_SIZE);
    }
}
