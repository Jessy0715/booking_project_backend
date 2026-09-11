package com.jessy.booking_project.controller;

import com.jessy.booking_project.dto.response.ApiResponse;
import com.jessy.booking_project.dto.response.BookingResponse;
import com.jessy.booking_project.entity.BookingStatus;
import com.jessy.booking_project.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** status 直接收 enum：EnumConverterConfig 負責 "pending" → PENDING，錯值自動 400。 */
    @GetMapping
    public ApiResponse<List<BookingResponse>> listBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long roomId) {
        return ApiResponse.ok(bookingService.search(userId, status, roomId));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getBooking(@PathVariable Long id) {
        return ApiResponse.ok(bookingService.getById(id));
    }
}
