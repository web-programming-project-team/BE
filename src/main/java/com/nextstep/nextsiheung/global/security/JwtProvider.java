package com.nextstep.nextsiheung.global.security;

import com.nextstep.nextsiheung.global.common.enums.Role;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Access 토큰(JWT, HS256) 발급·검증. Refresh 토큰은 JWT가 아니라 무작위 문자열이라 AuthService에서 다룬다
@Component
public class JwtProvider {

    private static final String ROLE = "role";

    private final SecretKey key;
    private final Duration accessValidity;

    public JwtProvider(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.access-token-validity-seconds}") long accessValiditySeconds) {
        this.key = toKey(secret);
        this.accessValidity = Duration.ofSeconds(accessValiditySeconds);
    }

    public String createAccessToken(Long userId, Role role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(ROLE, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessValidity)))
                .signWith(key)
                .compact();
    }

    public long accessValiditySeconds() {
        return accessValidity.toSeconds();
    }

    // 만료면 TOKEN_EXPIRED, 서명·형식 오류면 UNAUTHORIZED
    public AuthUser parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return new AuthUser(Long.valueOf(claims.getSubject()), Role.valueOf(claims.get(ROLE, String.class)));
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException(ErrorCode.UNAUTHORIZED);
        }
    }

    private static SecretKey toKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "app.jwt.secret은 256비트 이상 Base64 문자열이어야 합니다 (예: openssl rand -base64 32)", e);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        private final ErrorCode errorCode;

        public InvalidTokenException(ErrorCode errorCode) {
            super(errorCode.getMessage());
            this.errorCode = errorCode;
        }

        public ErrorCode getErrorCode() {
            return errorCode;
        }
    }
}
