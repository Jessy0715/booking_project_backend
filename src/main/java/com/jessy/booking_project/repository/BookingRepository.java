package com.jessy.booking_project.repository;

import com.jessy.booking_project.entity.Booking;
import com.jessy.booking_project.entity.BookingStatus;
import com.jessy.booking_project.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

/**
 * 多了 JpaSpecificationExecutor：提供 findAll(Specification, Sort)，
 * 讓「三個可選條件任意組合」不用寫 8 個方法。
 */
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /**
     * 找同棚同天同時段、狀態在指定集合內的第一筆。
     * Room_Id 的底線 = 走關聯 room 的 id，不會載入整個 Room。
     */
    Optional<Booking> findFirstByRoom_IdAndBookingDateAndTimeSlotAndStatusIn(
            Long roomId, LocalDate bookingDate, TimeSlot timeSlot, Collection<BookingStatus> statuses);
}
