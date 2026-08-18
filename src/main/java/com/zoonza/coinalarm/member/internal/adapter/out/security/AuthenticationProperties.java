package com.zoonza.coinalarm.member.internal.adapter.out.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("member.authentication")
public record AuthenticationProperties(
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String jwtSecret,
        long refreshTokenCacheMaximumSize,
        boolean refreshTokenCookieSecure
) {
    public AuthenticationProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("JWT issuer는 빈 값일 수 없습니다.");
        }
        if (accessTokenTtl == null || accessTokenTtl.isNegative() || accessTokenTtl.isZero()) {
            throw new IllegalArgumentException("액세스 토큰 만료 시간은 양수여야 합니다.");
        }
        if (refreshTokenTtl == null || refreshTokenTtl.isNegative() || refreshTokenTtl.isZero()) {
            throw new IllegalArgumentException("리프레시 토큰 만료 시간은 양수여야 합니다.");
        }
        if (jwtSecret == null || jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT 비밀 키는 32바이트 이상이어야 합니다.");
        }
        if (refreshTokenCacheMaximumSize <= 0) {
            throw new IllegalArgumentException("리프레시 토큰 캐시 최대 크기는 양수여야 합니다.");
        }
    }
}
