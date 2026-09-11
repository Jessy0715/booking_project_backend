package com.jessy.booking_project.repository;

import com.jessy.booking_project.entity.Booking;
import com.jessy.booking_project.entity.BookingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * 動態查詢條件。每個參數為 null 就略過，不為 null 就加一條 AND。
 *
 * <p>放在 repository/ 而不是 service/：它描述的是「怎麼查」，不是業務規則。
 */
public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    
    /*三個可選條件變 WHERE
    filter(null, PENDING, 1)
        → userId 是 null → 跳過
        → status 有值   → WHERE status = 'PENDING'
        → roomId 有值   → AND room_id = 1
    */
    public static Specification<Booking> filter(Long userId, BookingStatus status, Long roomId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (roomId != null) {
                // 走關聯的 id，不會觸發載入整個 Room
                predicates.add(cb.equal(root.get("room").get("id"), roomId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
