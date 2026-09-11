package com.jessy.booking_project.entity;

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
 * 使用者。對應資料表 users。
 *
 * <p>password 存 BCrypt 雜湊，絕不明文。這個欄位不可以出現在任何 Response DTO。
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** 一般使用者。 */
    public static final String ROLE_USER = "user";

    /** 後台管理員。 */
    public static final String ROLE_ADMIN = "admin";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String account;

    /** BCrypt 雜湊，長度固定 60，但留 100 給未來換演算法。 */
    @Column(nullable = false, length = 100)
    private String password;

    /** 契約用小寫字串 user / admin。 */
    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (role == null) {
            role = ROLE_USER;
        }
    }
}
