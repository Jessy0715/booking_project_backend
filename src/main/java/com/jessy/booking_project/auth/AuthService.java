package com.jessy.booking_project.auth;

import com.jessy.booking_project.auth.dto.AuthResponse;
import com.jessy.booking_project.auth.dto.LoginRequest;
import com.jessy.booking_project.auth.dto.RegisterRequest;
import com.jessy.booking_project.auth.dto.UserResponse;
import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import com.jessy.booking_project.security.AuthPrincipal;
import com.jessy.booking_project.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 註冊、登入、登出。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String account = request.account().trim();

        // 帳號不存在與密碼錯誤丟同一個例外，避免攻擊者從訊息差異篩出存在的帳號
        User user = userRepository.findByAccount(account)
                .orElseThrow(() -> {
                    // WARN 不是 ERROR：單次失敗很正常（打錯字），但連續失敗是暴力破解的訊號。
                    // 只記帳號，絕對不記密碼
                    log.warn("登入失敗（帳號不存在）：account={}", account);
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });

        // 明文 vs 雜湊（用 matches），密碼對就 true，不對丟例外
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("登入失敗（密碼錯誤）：account={}, uid={}", account, user.getId());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("登入成功：account={}, uid={}, role={}", account, user.getId(), user.getRole());
        return userMapper.toAuthResponse(user, token);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String account = request.account().trim();

        // 應用層先擋一次，給前端好的中文訊息。
        // 但這擋不住併發（兩個請求同時通過檢查），真正的保證是 users.account 的 UNIQUE 約束。
        if (userRepository.existsByAccount(account)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        String hashed = passwordEncoder.encode(request.password());
        User saved = userRepository.save(userMapper.toEntity(request, hashed));
        return userMapper.toResponse(saved);
    }

    /**
     * 登出。JWT 無狀態，這裡不查、不改任何資料表 —— token 本身直到過期前都還「有效」，
     * 只是前端已經把它丟掉不再送出。這支唯一實際做的事是留一筆稽核 log。
     */
    public void logout(AuthPrincipal me) {
        log.info("使用者登出：account={}, uid={}", me.account(), me.uid());
    }

    /**
     * 刪掉所有一般使用者，保留 admin。只給 Demo 站每日重置用。
     *
     * <p>保留 admin 是因為 register 只能建 user 角色，刪掉就沒人能進後台了。
     *
     * @return 刪了幾個帳號
     */
    @Transactional
    public int deleteNonAdminUsers() {
        return userRepository.deleteByRoleNot(User.ROLE_ADMIN);
    }
}
