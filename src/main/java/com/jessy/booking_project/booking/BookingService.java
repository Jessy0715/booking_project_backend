package com.jessy.booking_project.booking;

import com.jessy.booking_project.auth.User;
import com.jessy.booking_project.booking.dto.BookingCreateRequest;
import com.jessy.booking_project.booking.dto.BookingResponse;
import com.jessy.booking_project.booking.dto.BookingReviewRequest;
import com.jessy.booking_project.booking.dto.SlotAvailabilityResponse;
import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import com.jessy.booking_project.room.Room;
import com.jessy.booking_project.room.RoomService;
import com.jessy.booking_project.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    /** 「佔用時段」的狀態。rejected 不算，被退回後別人還能約。 */
    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.APPROVED);

    private final BookingRepository bookingRepository;
    private final RoomService roomService;
    private final BookingMapper bookingMapper;

    /** 契約：三個條件可選、AND 組合、created_at 由新到舊、不分頁。 */
    @Transactional(readOnly = true)
    public List<BookingResponse> search(Long userId, BookingStatus status, Long roomId) {
        List<Booking> bookings = bookingRepository.findAll(
                BookingSpecifications.filter(userId, status, roomId),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // Mapper 裡的 getRoom() 必須在這個 @Transactional 內執行，
        // 離開交易後 LAZY 關聯就撈不到了（open-in-view 已關）。
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        return bookingMapper.toResponse(booking);
    }

    /**
     * 某棚某天三個時段各自的狀態。
     *
     * <p>這個回應沒有對應的 Entity，所以不走 Mapper —— 它是「算」出來的，不是「轉」出來的。
     */
    @Transactional(readOnly = true)
    public SlotAvailabilityResponse getSlotAvailability(Long roomId, LocalDate date) {
        roomService.requireRoom(roomId);   // 不存在會丟 404

        // 一次查完這天所有有效預約，再依時段分組。舊版是三個時段各查一次。
        // 唯一索引保證同一時段最多一筆有效預約，所以 key 不會重複；merge 只是保險。
        Map<TimeSlot, BookingStatus> taken = bookingRepository
                .findByRoom_IdAndBookingDateAndStatusIn(roomId, date, ACTIVE_STATUSES)
                .stream()
                .collect(Collectors.toMap(Booking::getTimeSlot, Booking::getStatus, (a, b) -> a));

        return new SlotAvailabilityResponse(
                statusOf(taken, TimeSlot.MORNING),
                statusOf(taken, TimeSlot.AFTERNOON),
                statusOf(taken, TimeSlot.NIGHT));
    }

    /** 該時段有預約 → 回它的狀態（pending / approved）；沒有 → available。 */
    private String statusOf(Map<TimeSlot, BookingStatus> taken, TimeSlot slot) {
        BookingStatus status = taken.get(slot);
        return status == null ? SlotAvailabilityResponse.AVAILABLE : status.value();
    }

    @Transactional
    public BookingResponse create(BookingCreateRequest request, AuthPrincipal me) {
        Room room = roomService.requireRoom(request.roomId()); //這裡是跨模組改走service 的用法 (在 booking 使用room)
        TimeSlot slot = TimeSlot.fromValue(request.timeSlot());

        // 衝突檢查：同棚 + 同天 + 同時段，只要有 pending 或 approved 就不能再訂
        bookingRepository.findFirstByRoom_IdAndBookingDateAndTimeSlotAndStatusIn(
                        room.getId(), request.date(), slot, ACTIVE_STATUSES)
                .ifPresent(existing -> {
                    // 依對方的狀態回不同訊息（契約規定兩句不同的中文）
                    ErrorCode code = existing.getStatus() == BookingStatus.APPROVED
                            ? ErrorCode.BOOKING_SLOT_APPROVED
                            : ErrorCode.BOOKING_SLOT_PENDING;
                    throw new BusinessException(code);
                });

        Booking saved = bookingRepository.save(bookingMapper.toEntity(request, room, me.uid()));
        return bookingMapper.toResponse(saved);
    }

    /** 審核。Controller 已限 admin，這裡只管狀態機。 */
    @Transactional
    public BookingResponse review(Long id, BookingReviewRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        // 狀態機：只有 PENDING 能被審核，approved / rejected 都是終點，不能再改
        // booking.getStatus() 輸出的值是 'BookingStatus.xxxx' ，非xxxx
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_REVIEWED);
        }

        booking.setStatus(BookingStatus.fromValue(request.status()));
        return bookingMapper.toResponse(booking);
    }

    /** 取消。本人或 admin 才能取消，且只能取消 pending 的。 */
    @Transactional
    public void cancel(Long id, AuthPrincipal me) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        // 本人檢查（防 IDOR）：admin 可以取消任何人的；一般 user 只能取消自己的
        boolean isAdmin = User.ROLE_ADMIN.equals(me.role());
        // 訂的人 == 登入的人嗎？
        // Objects.equals 能安全處理 userId 是 null 的舊資料，不會 NPE (NullPointerException)
        boolean isOwner = Objects.equals(booking.getUserId(), me.uid());

        if (!isAdmin && !isOwner) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_OWNER);
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_PENDING);
        }
        bookingRepository.delete(booking);
    }
}
