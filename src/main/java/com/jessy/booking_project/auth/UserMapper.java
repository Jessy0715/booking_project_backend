package com.jessy.booking_project.auth;

import com.jessy.booking_project.auth.dto.AuthResponse;
import com.jessy.booking_project.auth.dto.RegisterRequest;
import com.jessy.booking_project.auth.dto.UserResponse;
import org.springframework.stereotype.Component;

/** User Entity ↔ Auth 相關 DTO。 */
@Component
public class UserMapper {

    /**
     * Request → Entity。password 傳「已經雜湊過」的值，
     * 雜湊動作留在 Service，Mapper 不碰安全邏輯。
     */
    public User toEntity(RegisterRequest request, String hashedPassword) {
        return User.builder()
                .account(request.account().trim())
                .password(hashedPassword)
                .role(User.ROLE_USER)
                .build();
    }

    /** 登入回應：同樣三個欄位再加 token。 */
    public AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(user.getId(), user.getAccount(), user.getRole(), token);
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getAccount(),
                user.getRole());
    }
}
