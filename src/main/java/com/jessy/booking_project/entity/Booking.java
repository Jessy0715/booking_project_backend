package com.jessy.booking_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** 預約。對應資料表 bookings。 
 * Builder 會產生全參的建構子，要靠 AllArgsConstructor 才能build
*/
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * LAZY：撈 Booking 時不會順便撈 Room，要用到 getRoom() 才查。
     * 這正是 N+1 的來源 —— 10 筆預約各自去查一次 Room。
     * 一間 Room 有多筆 Booking
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /** 可為 null：舊版允許未登入預約。不做 @ManyToOne User，跟舊 schema 一樣只存 id。 */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    /** ⚠️ date 是 PostgreSQL 型別名，欄位改名。 */
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    /** STRING 不是 ORDINAL：存 "MORNING" 而不是 0，enum 順序改了資料也不會錯位。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false, length = 20)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // PrePersist 用法在於 Hibernate 在送出 INSERT 之前，會先呼叫這個方法。
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = BookingStatus.PENDING;
        }
        if (reason == null) {
            reason = "";
        }
        if (userName == null) {
            userName = "";
        }
    }
}
