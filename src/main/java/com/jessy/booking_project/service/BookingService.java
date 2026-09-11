package com.jessy.booking_project.service;

import com.jessy.booking_project.dto.request.BookingCreateRequest;
import com.jessy.booking_project.dto.request.BookingReviewRequest;
import com.jessy.booking_project.dto.response.BookingResponse;
import com.jessy.booking_project.entity.Booking;
import com.jessy.booking_project.entity.BookingStatus;
import com.jessy.booking_project.entity.Room;
import com.jessy.booking_project.entity.TimeSlot;
import com.jessy.booking_project.entity.User;
import com.jessy.booking_project.exception.BookingConflictException;
import com.jessy.booking_project.exception.BookingNotFoundException;
import com.jessy.booking_project.exception.BookingStateException;
import com.jessy.booking_project.exception.RoomNotFoundException;
import com.jessy.booking_project.mapper.BookingMapper;
import com.jessy.booking_project.repository.BookingRepository;
import com.jessy.booking_project.repository.BookingSpecifications;
import com.jessy.booking_project.repository.RoomRepository;
import com.jessy.booking_project.security.AuthPrincipal;
import org.springframework.security.access.AccessDeniedException;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
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
                .orElseThrow(BookingNotFoundException::new);
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse create(BookingCreateRequest request, AuthPrincipal me) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(RoomNotFoundException::new);
        TimeSlot slot = TimeSlot.fromValue(request.timeSlot());

        // 衝突檢查：同棚 + 同天 + 同時段，只要有 pending 或 approved 就不能再訂
        bookingRepository.findFirstByRoom_IdAndBookingDateAndTimeSlotAndStatusIn(
                        room.getId(), request.date(), slot,
                        List.of(BookingStatus.PENDING, BookingStatus.APPROVED))
                .ifPresent(existing -> {
                    // 依對方的狀態回不同訊息（契約規定兩句不同的中文）
                    String message = existing.getStatus() == BookingStatus.APPROVED
                            ? BookingConflictException.APPROVED_MESSAGE
                            : BookingConflictException.PENDING_MESSAGE;
                    throw new BookingConflictException(message);
                });

        Booking saved = bookingRepository.save(bookingMapper.toEntity(request, room, me.uid()));
        return bookingMapper.toResponse(saved);
    }

    /** 審核。Controller 已限 admin，這裡只管狀態機。 */
    @Transactional
    public BookingResponse review(Long id, BookingReviewRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(BookingNotFoundException::new);

        // 狀態機：只有 PENDING 能被審核，approved / rejected 都是終點，不能再改
        // booking.getStatus() 輸出的值是 'BookingStatus.xxxx' ，非xxxx
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingStateException("此預約已審核完畢，無法再次變更");
        }

        booking.setStatus(BookingStatus.fromValue(request.status()));
        return bookingMapper.toResponse(booking);
    }

    /** 取消。本人或 admin 才能取消，且只能取消 pending 的。 */
    @Transactional
    public void cancel(Long id, AuthPrincipal me) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(BookingNotFoundException::new);

        // 本人檢查（防 IDOR）：admin 可以取消任何人的；一般 user 只能取消自己的
        boolean isAdmin = User.ROLE_ADMIN.equals(me.role());
        // 訂的人 == 登入的人嗎？
        // Objects.equals 能安全處理 userId 是 null 的舊資料，不會 NPE (NullPointerException)
        boolean isOwner = Objects.equals(booking.getUserId(), me.uid());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("不能取消別人的預約");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingStateException("只能取消審核中 (pending) 的預約");
        }
        bookingRepository.delete(booking);
    }
}
