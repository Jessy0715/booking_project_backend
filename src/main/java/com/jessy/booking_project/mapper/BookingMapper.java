package com.jessy.booking_project.mapper;

import com.jessy.booking_project.dto.request.BookingCreateRequest;
import com.jessy.booking_project.dto.response.BookingResponse;
import com.jessy.booking_project.entity.Booking;
import com.jessy.booking_project.entity.Room;
import com.jessy.booking_project.entity.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class BookingMapper {

    private static final DateTimeFormatter CREATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    /** Request → 新 Entity。Room 由 Service 查好傳進來；userId 來自 token 不是 body。 */
    public Booking toEntity(BookingCreateRequest request, Room room, Long userId) {
        return Booking.builder()
                .room(room)
                .userId(userId)
                .userName(request.userName() == null ? "" : request.userName().trim())
                .bookingDate(request.date())
                .timeSlot(TimeSlot.fromValue(request.timeSlot()))
                .reason(request.reason() == null ? "" : request.reason().trim())
                .build();
    }

    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getId(),        // ← 走關聯。LAZY 時這裡才發 SQL 查 Room
                booking.getRoom().getTitle(),
                booking.getUserId(),
                booking.getUserName(),
                booking.getBookingDate().toString(),   // LocalDate.toString() = yyyy-MM-dd
                booking.getTimeSlot().value(),         // MORNING → "morning"
                booking.getReason(),
                booking.getStatus().value(),           // PENDING → "pending"
                CREATED_AT_FORMAT.format(booking.getCreatedAt()));
    }
}
