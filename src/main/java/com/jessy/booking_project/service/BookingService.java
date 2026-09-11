package com.jessy.booking_project.service;

import com.jessy.booking_project.dto.response.BookingResponse;
import com.jessy.booking_project.entity.Booking;
import com.jessy.booking_project.entity.BookingStatus;
import com.jessy.booking_project.exception.BookingNotFoundException;
import com.jessy.booking_project.mapper.BookingMapper;
import com.jessy.booking_project.repository.BookingRepository;
import com.jessy.booking_project.repository.BookingSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
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
}
