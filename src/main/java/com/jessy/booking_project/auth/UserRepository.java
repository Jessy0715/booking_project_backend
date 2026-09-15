package com.jessy.booking_project.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 7b 登入時用。 */
    Optional<User> findByAccount(String account);

    /** 註冊時檢查帳號有沒有被用過，只查 boolean 比撈整筆輕。 */
    boolean existsByAccount(String account);

    /** 刪掉所有非指定角色的使用者。Demo 站每日重置用，保留 admin。 */
    int deleteByRoleNot(String role);
}
