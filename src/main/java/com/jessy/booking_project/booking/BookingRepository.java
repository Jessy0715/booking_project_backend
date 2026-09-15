package com.jessy.booking_project.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 多了 JpaSpecificationExecutor：提供 findAll(Specification, Sort)，
 * 讓「三個可選條件任意組合」不用寫 8 個方法。
 */
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /**
     * 找同棚同天同時段、狀態在指定集合內的第一筆。
     * Room_Id 的底線 = 走關聯 room 的 id，不會載入整個 Room。
     * findFirstBy Room_Id And BookingDate And TimeSlot And StatusIn
     * │   │       │       │   │           │   │        │   │
     * │   │       │       │   │           │   │        │   └─ IN (...)：值在集合裡
     * │   │       │       │   │           │   │        └─ 欄位 status
     * │   │       │       │   │           │   └─ AND
     * │   │       │       │   │           └─ 欄位 timeSlot
     * │   │       │       │   └─ AND
     * │   │       │       └─ 欄位 bookingDate
     * │   │       └─ 走關聯 room，取它的 id（底線 = 進入關聯物件）
     * │   └─ 條件開始
     * └─ 只取第一筆（回 Optional）
     */
    Optional<Booking> findFirstByRoom_IdAndBookingDateAndTimeSlotAndStatusIn(
            Long roomId, LocalDate bookingDate, TimeSlot timeSlot, Collection<BookingStatus> statuses);

    /** 某棚某天所有有效預約（不分時段）。一次查完，Service 再分到三個時段 —— 舊版是查三次。 */
    List<Booking> findByRoom_IdAndBookingDateAndStatusIn(
            Long roomId, LocalDate bookingDate, Collection<BookingStatus> statuses);

    /**
     * 真正從資料表刪光，包含已軟刪除的。只給 Demo 站每日重置用。
     *
     * <p>不能用 {@code deleteAll()}：Entity 上的 @SQLDelete 會把它變成 UPDATE（軟刪除），
     * @SQLRestriction 又讓它看不見已軟刪除的資料 —— 兩者疊起來永遠清不乾淨。
     * 原生 SQL 完全繞過 Hibernate 的這兩層。
     */
    @Modifying
    @Query(value = "DELETE FROM bookings", nativeQuery = true)
    int hardDeleteAll();
}
