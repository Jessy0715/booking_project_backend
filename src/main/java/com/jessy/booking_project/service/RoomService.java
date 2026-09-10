package com.jessy.booking_project.service;

import com.jessy.booking_project.dto.request.RoomCreateRequest;
import com.jessy.booking_project.dto.response.RoomResponse;
import com.jessy.booking_project.entity.Room;
import com.jessy.booking_project.exception.RoomNotFoundException;
import com.jessy.booking_project.mapper.RoomMapper;
import com.jessy.booking_project.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 場地的業務邏輯。進出都是 DTO，Entity 只活在這個 class 內部。 */
@Service
@RequiredArgsConstructor
public class RoomService {

    /** 單頁筆數上限，擋掉 ?pageSize=999999 一次撈全表。 */
    private static final int MAX_PAGE_SIZE = 100;

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(RoomNotFoundException::new);
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
                .orElseThrow(RoomNotFoundException::new);

        // 不用呼叫 save()：交易內撈出的 Entity 受 Hibernate 管理，
        // 方法結束時它會自己比對有沒有被改過並發 UPDATE（髒檢查）。
        roomMapper.applyRequest(room, request);

        return roomMapper.toResponse(room);
    }

    @Transactional
    public void delete(Long id) {
        // existsById 只查一個 boolean，比 findById 搬回整筆資料輕。
        if (!roomRepository.existsById(id)) {
            throw new RoomNotFoundException();
        }
        roomRepository.deleteById(id);
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
