package com.jessy.booking_project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 每個請求跑一次：有帶 token 就驗，驗過就把身分放進 SecurityContext。
 *
 * <p><b>這個 filter 不做「擋不擋」的決定</b> —— 它只負責「你是誰」。
 * 擋不擋是後面 AuthorizationFilter 依 SecurityConfig 的規則決定的。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                SecurityContextHolder.getContext().setAuthentication(toAuthentication(claims));
            } catch (JwtException | IllegalArgumentException e) {
                // token 壞掉或過期：不在這裡回 401，只是「沒有身分」，
                // 讓後面的規則決定這個端點需不需要身分。
                log.debug("JWT 驗證失敗：{}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            return null;
        }
        return header.substring(PREFIX.length()).trim();
    }

    /**
     * claims → Spring Security 認得的 Authentication。
     *
     * <p>role 存的是小寫 "admin"，但 hasRole("ADMIN") 找的是權限字串 "ROLE_ADMIN"，
     * 所以要轉大寫加前綴。
     */
    private UsernamePasswordAuthenticationToken toAuthentication(Claims claims) {
        String account = claims.getSubject();
        Long uid = claims.get("uid", Number.class).longValue();
        String role = claims.get("role", String.class);

        AuthPrincipal principal = new AuthPrincipal(uid, account, role);
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

        // 第二個參數是 credentials（密碼），驗證已經完成，不需要也不該再帶
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}
