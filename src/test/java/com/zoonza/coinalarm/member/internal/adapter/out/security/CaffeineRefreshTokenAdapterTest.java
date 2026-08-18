package com.zoonza.coinalarm.member.internal.adapter.out.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zoonza.coinalarm.member.internal.application.dto.RefreshTokenSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineRefreshTokenAdapterTest {
    private static final Instant ISSUED_AT = Instant.parse("2026-08-18T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-08-25T00:00:00Z");

    private Cache<String, RefreshTokenSession> cache;
    private CaffeineRefreshTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        cache = Caffeine.newBuilder().maximumSize(100).build();
        AuthenticationProperties properties = new AuthenticationProperties(
                "coin-alarm",
                Duration.ofMinutes(10),
                Duration.ofDays(7),
                "test-secret-key-that-is-at-least-32-bytes",
                100,
                false
        );
        adapter = new CaffeineRefreshTokenAdapter(cache, properties);
    }

    @Test
    @DisplayName("불투명 리프레시 토큰을 발급하고 한 번만 소비한다")
    void issuesOpaqueTokenAndConsumesItOnlyOnce() {
        String token = adapter.issue(1L, ISSUED_AT);

        assertThat(token).hasSize(43);
        assertThat(cache.asMap()).doesNotContainKey(token);
        assertThat(adapter.consume(token)).contains(new RefreshTokenSession(
                1L,
                EXPIRES_AT
        ));
        assertThat(adapter.consume(token)).isEmpty();
    }

    @Test
    @DisplayName("발급한 리프레시 토큰을 폐기한다")
    void revokesIssuedToken() {
        String token = adapter.issue(1L, ISSUED_AT);

        adapter.revoke(token);

        assertThat(adapter.consume(token)).isEmpty();
    }

    @Test
    @DisplayName("같은 회원에게도 서로 다른 리프레시 토큰을 발급한다")
    void issuesDifferentTokensForTheSameMember() {
        String first = adapter.issue(1L, ISSUED_AT);
        String second = adapter.issue(1L, ISSUED_AT);

        assertThat(first).isNotEqualTo(second);
    }
}
