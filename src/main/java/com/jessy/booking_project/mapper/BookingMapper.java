package com.jessy.booking_project.mapper;

import com.jessy.booking_project.dto.response.BookingResponse;
import com.jessy.booking_project.entity.Booking;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class BookingMapper {

    private static final DateTimeFormatter CREATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

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
