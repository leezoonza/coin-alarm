package com.zoonza.coinalarm.member.internal.adapter.out.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.zoonza.coinalarm.member.internal.application.dto.RefreshTokenSession;
import com.zoonza.coinalarm.member.internal.application.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CaffeineRefreshTokenAdapter implements RefreshTokenPort {
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final Cache<String, RefreshTokenSession> cache;
    private final AuthenticationProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String issue(Long memberId, Instant issuedAt) {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];

        secureRandom.nextBytes(tokenBytes);

        String refreshToken = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        cache.put(
                hash(refreshToken),
                new RefreshTokenSession(
                        memberId,
                        issuedAt.plus(properties.refreshTokenTtl())
                )
        );

        return refreshToken;
    }

    @Override
    public Optional<RefreshTokenSession> consume(String refreshToken) {
        return Optional.ofNullable(cache.asMap().remove(hash(refreshToken)));
    }

    @Override
    public void revoke(String refreshToken) {
        cache.invalidate(hash(refreshToken));
    }

    private String hash(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
        }
    }
}
