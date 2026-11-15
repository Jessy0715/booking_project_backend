package com.jessy.booking_project.booking;

import com.jessy.booking_project.booking.dto.BookingCreateRequest;
import com.jessy.booking_project.booking.dto.BookingResponse;
import com.jessy.booking_project.booking.dto.BookingReviewRequest;
import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import com.jessy.booking_project.room.Room;
import com.jessy.booking_project.room.RoomService;
import com.jessy.booking_project.security.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** BookingService 單元測試：Repository 與 RoomService 全部 mock，Mapper 用真的（純轉換、無依賴）。 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final Long ROOM_ID = 1L;
    private static final Long BOOKING_ID = 10L;
    private static final Long OWNER_UID = 100L;
    private static final Long OTHER_UID = 200L;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 15);

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomService roomService;

    private BookingService service;
    private Room room;

    @BeforeEach
    void setUp() {
        service = new BookingService(bookingRepository, roomService, new BookingMapper());
        room = new Room();
        room.setId(ROOM_ID);
        room.setTitle("A 棚");
    }

    private BookingCreateRequest createRequest() {
        return new BookingCreateRequest(ROOM_ID, "王小明", DATE, "morning", "商品拍攝");
    }

    private Booking booking(BookingStatus status, Long userId) {
        return Booking.builder()
                .id(BOOKING_ID)
                .room(room)
                .userId(userId)
                .userName("王小明")
                .bookingDate(DATE)
                .timeSlot(TimeSlot.MORNING)
                .reason("商品拍攝")
                .status(status)
                .createdAt(Instant.parse("2026-09-01T00:00:00Z"))
                .build();
    }

    private AuthPrincipal user(Long uid) {
        return new AuthPrincipal(uid, "user" + uid, "user");
    }

    private AuthPrincipal admin() {
        return new AuthPrincipal(1L, "admin", "admin");
    }

    // ---------- 時段衝突 ----------

    @Test
    @DisplayName("create：時段已有 approved 預約 → BOOKING_SLOT_APPROVED 且不儲存")
    void create_whenSlotApproved_throwsSlotApproved() {
        when(roomService.requireRoom(ROOM_ID)).thenReturn(room);
        when(bookingRepository.findFirstByRoom_IdAndBookingDateAndTimeSlotAndStatusIn(
                any(), any(), any(), any()))
                .thenReturn(Optional.of(booking(BookingStatus.APPROVED, OTHER_UID)));

        assertThatThrownBy(() -> service.create(createRequest(), user(OWNER_UID)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_SLOT_APPROVED));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("create：時段已有 pending 預約 → BOOKING_SLOT_PENDING 且不儲存")
    void create_whenSlotPending_throwsSlotPending() {
        when(roomService.requireRoom(ROOM_ID)).thenReturn(room);
        when(bookingRepository.findFirstByRoom_IdAndBookingDateAndTimeSlotAndStatusIn(
                any(), any(), any(), any()))
                .thenReturn(Optional.of(booking(BookingStatus.PENDING, OTHER_UID)));

        assertThatThrownBy(() -> service.create(createRequest(), user(OWNER_UID)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_SLOT_PENDING));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("create：時段空閒 → 以 token 的 uid 儲存，狀態回應為 pending")
    void create_whenSlotFree_savesWithTokenUid() {
        when(roomService.requireRoom(ROOM_ID)).thenReturn(room);
        when(bookingRepository.findFirstByRoom_IdAndBookingDateAndTimeSlotAndStatusIn(
                any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(BOOKING_ID);
            b.setStatus(BookingStatus.PENDING);
            b.setCreatedAt(Instant.parse("2026-09-01T00:00:00Z"));
            return b;
        });

        BookingResponse response = service.create(createRequest(), user(OWNER_UID));

        assertThat(response.id()).isEqualTo(BOOKING_ID);
        assertThat(response.userId()).isEqualTo(OWNER_UID);
        assertThat(response.status()).isEqualTo("pending");
    }

    @Test
    @DisplayName("create：衝突檢查只看 pending 與 approved，rejected 不佔時段")
    void create_conflictCheck_onlyQueriesPendingAndApproved() {
        when(roomService.requireRoom(ROOM_ID)).thenReturn(room);
        when(bookingRepository.findFirstByRoom_IdAndBookingDateAndTimeSlotAndStatusIn(
                ROOM_ID, DATE, TimeSlot.MORNING,
                List.of(BookingStatus.PENDING, BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setStatus(BookingStatus.PENDING);
            b.setCreatedAt(Instant.now());
            return b;
        });

        service.create(createRequest(), user(OWNER_UID));

        verify(bookingRepository).save(any(Booking.class));
    }

    // ---------- 狀態機：只有 pending 能審核 ----------

    @Test
    @DisplayName("review：pending 核准 → 狀態變 approved")
    void review_whenPending_approves() {
        Booking pending = booking(BookingStatus.PENDING, OWNER_UID);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(pending));

        BookingResponse response = service.review(
                BOOKING_ID, new BookingReviewRequest("approved"), admin());

        assertThat(response.status()).isEqualTo("approved");
        assertThat(pending.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("review：pending 退回 → 狀態變 rejected")
    void review_whenPending_rejects() {
        Booking pending = booking(BookingStatus.PENDING, OWNER_UID);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(pending));

        BookingResponse response = service.review(
                BOOKING_ID, new BookingReviewRequest("rejected"), admin());

        assertThat(response.status()).isEqualTo("rejected");
    }

    @Test
    @DisplayName("review：已 approved 再審核 → BOOKING_ALREADY_REVIEWED 且狀態不變")
    void review_whenApproved_throwsAlreadyReviewed() {
        Booking approved = booking(BookingStatus.APPROVED, OWNER_UID);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> service.review(
                BOOKING_ID, new BookingReviewRequest("rejected"), admin()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_ALREADY_REVIEWED));
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("review：已 rejected 再審核 → BOOKING_ALREADY_REVIEWED 且狀態不變")
    void review_whenRejected_throwsAlreadyReviewed() {
        Booking rejected = booking(BookingStatus.REJECTED, OWNER_UID);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(rejected));

        assertThatThrownBy(() -> service.review(
                BOOKING_ID, new BookingReviewRequest("approved"), admin()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_ALREADY_REVIEWED));
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    @DisplayName("review：預約不存在 → BOOKING_NOT_FOUND")
    void review_whenNotFound_throwsNotFound() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.review(
                BOOKING_ID, new BookingReviewRequest("approved"), admin()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_NOT_FOUND));
    }

    // ---------- 本人才能取消 ----------

    @Test
    @DisplayName("cancel：本人取消自己的 pending 預約 → 刪除成功")
    void cancel_whenOwnerAndPending_deletes() {
        Booking pending = booking(BookingStatus.PENDING, OWNER_UID);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(pending));

        service.cancel(BOOKING_ID, user(OWNER_UID));

        verify(bookingRepository).delete(pending);
    }

    @Test
    @DisplayName("cancel：非本人且非 admin → BOOKING_NOT_OWNER 且不刪除")
    void cancel_whenNotOwner_throwsBookingNotOwner() {
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking(BookingStatus.PENDING, OWNER_UID)));

        assertThatThrownBy(() -> service.cancel(BOOKING_ID, user(OTHER_UID)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_NOT_OWNER));
        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    @Test
    @DisplayName("cancel：預約沒有 userId（舊資料）時，一般使用者不能取消")
    void cancel_whenBookingHasNoUserId_throwsBookingNotOwner() {
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking(BookingStatus.PENDING, null)));

        assertThatThrownBy(() -> service.cancel(BOOKING_ID, user(OTHER_UID)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_NOT_OWNER));
    }

    @Test
    @DisplayName("cancel：admin 可取消別人的 pending 預約")
    void cancel_whenAdminAndNotOwner_deletes() {
        Booking pending = booking(BookingStatus.PENDING, OWNER_UID);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(pending));

        service.cancel(BOOKING_ID, admin());

        verify(bookingRepository).delete(pending);
    }

    @Test
    @DisplayName("cancel：本人取消已 approved 的預約 → BOOKING_NOT_PENDING 且不刪除")
    void cancel_whenOwnerButApproved_throwsNotPending() {
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking(BookingStatus.APPROVED, OWNER_UID)));

        assertThatThrownBy(() -> service.cancel(BOOKING_ID, user(OWNER_UID)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_NOT_PENDING));
        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    @Test
    @DisplayName("cancel：預約不存在 → BOOKING_NOT_FOUND")
    void cancel_whenNotFound_throwsNotFound() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(BOOKING_ID, user(OWNER_UID)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.BOOKING_NOT_FOUND));
    }
}
