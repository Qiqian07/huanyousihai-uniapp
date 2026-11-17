package com.ruoyi.hys.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 小程序会员 JWT 令牌（与后台管理员体系隔离，不依赖 Redis）
 */
@Component
public class HysTokenService {

    @Value("${hys.jwt.secret:hys-default-secret-key-change-me-in-production-2026}")
    private String secret;

    /** 7 天有效期 */
    private static final long EXPIRE_MS = 7L * 24 * 3600 * 1000;

    private SecretKey key() {
        byte[] bytes = new byte[32];
        byte[] src = secret.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(src, 0, bytes, 0, Math.min(src.length, bytes.length));
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("typ", "hys-member")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRE_MS))
                .signWith(key())
                .compact();
    }

    /** 校验并解析，无效或过期返回 null */
    public Long parseToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key()).build()
                    .parseSignedClaims(token).getPayload();
            if (!"hys-member".equals(claims.get("typ", String.class))) {
                return null;
            }
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}
