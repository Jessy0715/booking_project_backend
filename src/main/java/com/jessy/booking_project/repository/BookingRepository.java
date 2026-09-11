package com.jessy.booking_project.repository;

import com.jessy.booking_project.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 多了 JpaSpecificationExecutor：提供 findAll(Specification, Sort)，
 * 讓「三個可選條件任意組合」不用寫 8 個方法。
 */
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
}
