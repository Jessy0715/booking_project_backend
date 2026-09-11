package com.jessy.booking_project.security;

import com.jessy.booking_project.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * 產生與解析 JWT。放在 security/ 而不是 service/ —— 它是安全基礎設施，沒有業務邏輯。
 *
 * <p>7b 只做「產生」，7c 才加「解析驗證」。
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationSeconds;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds,
            @Value("${app.jwt.issuer}") String issuer) {
        // HS256 要求金鑰至少 256 bit（32 bytes），太短這行會直接丟例外
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
        this.issuer = issuer;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))

                // sub：這個 token 代表誰。用 account 而不是 id，log 看得懂
                .subject(user.getAccount())
                // uid：Service 要查「這筆資料是不是本人的」時用（階段 9 防 IDOR）
                .claim("uid", user.getId())
                // role：7c 的 filter 靠這個決定放不放行，不用查 DB
                .claim("role", user.getRole())

                .signWith(key)
                .compact();
    }

    /**
     * 驗簽 + 驗過期 + 驗 issuer，通過才回 claims。
     *
     * <p>任何一項不過就丟 {@code JwtException}（含子類 ExpiredJwtException、SignatureException），
     * 由呼叫端決定怎麼處理。
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
