package com.zoonza.coinalarm.member.internal.adapter.out.security;

import com.zoonza.coinalarm.member.internal.application.dto.IssuedAccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenConfigTest {
    private static final String ISSUER = "coin-alarm";
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(10);

    private JwtDecoder decoder;
    private JwtAccessTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        AuthenticationProperties properties = new AuthenticationProperties(
                ISSUER,
                ACCESS_TOKEN_TTL,
                Duration.ofDays(7),
                "test-secret-key-that-is-at-least-32-bytes",
                100,
                false
        );
        JwtTokenConfig config = new JwtTokenConfig();
        SecretKey secretKey = config.jwtSecretKey(properties);
        JwtEncoder encoder = config.jwtEncoder(secretKey);
        decoder = config.jwtDecoder(secretKey, properties);
        adapter = new JwtAccessTokenAdapter(encoder, properties);
    }

    @Test
    @DisplayName("리소스 서버 디코더가 발급된 액세스 토큰을 검증한다")
    void resourceServerDecoderValidatesIssuedAccessToken() {
        Instant issuedAt = Instant.now()
                .minusSeconds(1)
                .truncatedTo(ChronoUnit.SECONDS);

        IssuedAccessToken issued = adapter.issue(42L, issuedAt);
        Jwt jwt = decoder.decode(issued.value());

        assertThat(jwt.getClaimAsString("iss")).isEqualTo(ISSUER);
        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getExpiresAt()).isEqualTo(issuedAt.plus(ACCESS_TOKEN_TTL));
        assertThat(issued.expiresAt()).isEqualTo(issuedAt.plus(ACCESS_TOKEN_TTL));
    }
}
