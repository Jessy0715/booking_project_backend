package com.jessy.booking_project.room;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 場地。對應資料表 rooms。這是資料庫的形狀，不是 API 的形狀。
 * facilities 存逗號字串、價格是三個平坦欄位，由 RoomMapper 轉成 API 要的形狀。
 */
@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_img", nullable = false)
    private String roomImg;

    @Column(nullable = false)
    private String title;

    /** 契約的 JSON key 是 desc，但 desc 是 PostgreSQL 保留字，所以欄位改名。 */
    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "price_morning", nullable = false)
    private Integer priceMorning;

    @Column(name = "price_afternoon", nullable = false)
    private Integer priceAfternoon;

    @Column(name = "price_night", nullable = false)
    private Integer priceNight;

    private Integer capacity;

    private Integer area;

    private String floor;

    /** 逗號分隔，例如 {@code 閃燈組,柔光箱,背景架}。 */
    @Column(length = 500)
    private String facilities;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
