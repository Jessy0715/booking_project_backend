package com.jessy.booking_project.service;

import com.jessy.booking_project.dto.request.LoginRequest;
import com.jessy.booking_project.dto.request.RegisterRequest;
import com.jessy.booking_project.dto.response.AuthResponse;
import com.jessy.booking_project.dto.response.UserResponse;
import com.jessy.booking_project.entity.User;
import com.jessy.booking_project.exception.DuplicateAccountException;
import com.jessy.booking_project.exception.InvalidCredentialsException;
import com.jessy.booking_project.security.JwtTokenProvider;
import com.jessy.booking_project.mapper.UserMapper;
import com.jessy.booking_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 註冊與登入。7b 會在這裡加 login。 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 帳號不存在與密碼錯誤丟同一個例外，避免攻擊者從訊息差異篩出存在的帳號
        User user = userRepository.findByAccount(request.account().trim())
                .orElseThrow(InvalidCredentialsException::new);

        // 明文 vs 雜湊（用 matches），密碼對就 true，不對丟例外
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenProvider.generateToken(user);
        return userMapper.toAuthResponse(user, token);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String account = request.account().trim();

        // 應用層先擋一次，給前端好的中文訊息。
        // 但這擋不住併發（兩個請求同時通過檢查），真正的保證是 users.account 的 UNIQUE 約束。
        if (userRepository.existsByAccount(account)) {
            throw new DuplicateAccountException();
        }

        String hashed = passwordEncoder.encode(request.password());
        User saved = userRepository.save(userMapper.toEntity(request, hashed));
        return userMapper.toResponse(saved);
    }
}
