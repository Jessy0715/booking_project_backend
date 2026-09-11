package com.jessy.booking_project.booking;

import com.jessy.booking_project.booking.dto.BookingCreateRequest;
import com.jessy.booking_project.booking.dto.BookingResponse;
import com.jessy.booking_project.booking.dto.BookingReviewRequest;
import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.security.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** status 直接收 enum：EnumConverterConfig 負責 "pending" → PENDING，錯值自動 400。 */
    @GetMapping
    public ApiResponse<List<BookingResponse>> searchBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long roomId) {
        return ApiResponse.ok(bookingService.search(userId, status, roomId));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getBooking(@PathVariable Long id) {
        return ApiResponse.ok(bookingService.getById(id));
    }

    /** @AuthenticationPrincipal 直接拿到 filter 放進去的 AuthPrincipal，不查 DB。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request,
                                                      @AuthenticationPrincipal AuthPrincipal me) {
        return ApiResponse.ok(bookingService.create(request, me));
    }

    /** SecurityConfig 已限 PATCH /api/bookings/** 為 ADMIN。 */
    @PatchMapping("/{id}")
    public ApiResponse<BookingResponse> reviewBooking(@PathVariable Long id,
                                                      @Valid @RequestBody BookingReviewRequest request) {
        return ApiResponse.ok(bookingService.review(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelBooking(@PathVariable Long id,
                                           @AuthenticationPrincipal AuthPrincipal me) {
        bookingService.cancel(id, me);
        return ApiResponse.okMessage("預約已取消");
    }
}
