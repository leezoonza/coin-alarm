package com.zoonza.coinalarm.member.internal.application.service;

import com.zoonza.coinalarm.common.error.DomainException;
import com.zoonza.coinalarm.member.internal.application.dto.AuthenticationTokens;
import com.zoonza.coinalarm.member.internal.application.dto.IssuedAccessToken;
import com.zoonza.coinalarm.member.internal.application.dto.RefreshTokenSession;
import com.zoonza.coinalarm.member.internal.application.port.out.AccessTokenPort;
import com.zoonza.coinalarm.member.internal.application.port.out.RefreshTokenPort;
import com.zoonza.coinalarm.member.internal.domain.AuthenticationErrorCode;
import com.zoonza.coinalarm.member.internal.domain.Member;
import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import com.zoonza.coinalarm.member.internal.domain.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.zoonza.coinalarm.member.fixture.PasswordEncoderFixture.returning;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberAuthenticationServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-18T00:00:00Z");
    private static final Instant REGISTERED_AT = Instant.parse("2026-08-17T00:00:00Z");
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(10);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccessTokenPort accessTokenPort;
    @Mock
    private RefreshTokenPort refreshTokenPort;

    private MemberAuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new MemberAuthenticationService(
                Clock.fixed(NOW, ZoneOffset.UTC),
                memberRepository,
                passwordEncoder,
                accessTokenPort,
                refreshTokenPort
        );
    }

    @Test
    @DisplayName("올바른 로그인 정보로 로그인하고 토큰을 발급한다")
    void logsInWithValidCredentials() {
        Member member = persistedMember(1L, "zoonza", "password-hash");
        when(memberRepository.findByLoginId("zoonza")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("secret", "password-hash")).thenReturn(true);
        when(accessTokenPort.issue(1L, NOW))
                .thenReturn(new IssuedAccessToken("access-token", NOW.plus(ACCESS_TOKEN_TTL)));
        when(refreshTokenPort.issue(1L, NOW))
                .thenReturn("refresh-token");

        AuthenticationTokens tokens = service.login("zoonza", "secret");

        assertThat(tokens).isEqualTo(new AuthenticationTokens(
                "access-token",
                "refresh-token",
                "Bearer",
                ACCESS_TOKEN_TTL.toSeconds()
        ));
        assertThat(member.getLastLoginAt()).isEqualTo(NOW);
        assertThat(member.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("존재하지 않는 로그인 아이디이면 토큰을 발급하지 않는다")
    void rejectsUnknownLoginIdWithoutIssuingTokens() {
        when(memberRepository.findByLoginId("unknown")).thenReturn(Optional.empty());

        assertAuthenticationError(
                () -> service.login("unknown", "secret"),
                AuthenticationErrorCode.INVALID_CREDENTIALS
        );

        verify(passwordEncoder, never()).matches(any(), any());
        verify(accessTokenPort, never()).issue(any(), any());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 토큰을 발급하지 않는다")
    void rejectsIncorrectPasswordWithoutIssuingTokens() {
        Member member = persistedMember(1L, "zoonza", "password-hash");
        when(memberRepository.findByLoginId("zoonza")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "password-hash")).thenReturn(false);

        assertAuthenticationError(
                () -> service.login("zoonza", "wrong"),
                AuthenticationErrorCode.INVALID_CREDENTIALS
        );

        verify(accessTokenPort, never()).issue(any(), any());
        assertThat(member.getLastLoginAt()).isNull();
    }

    @Test
    @DisplayName("토큰을 리프레시하면 기존 리프레시 토큰을 새 토큰으로 교체한다")
    void rotatesRefreshTokenWhenRefreshing() {
        RefreshTokenSession session = new RefreshTokenSession(
                1L,
                NOW.plus(REFRESH_TOKEN_TTL)
        );
        when(refreshTokenPort.consume("old-refresh-token")).thenReturn(Optional.of(session));
        when(accessTokenPort.issue(1L, NOW))
                .thenReturn(new IssuedAccessToken("new-access-token", NOW.plus(ACCESS_TOKEN_TTL)));
        when(refreshTokenPort.issue(1L, NOW))
                .thenReturn("new-refresh-token");

        AuthenticationTokens tokens = service.refresh("old-refresh-token");

        assertThat(tokens.accessToken()).isEqualTo("new-access-token");
        assertThat(tokens.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("리프레시 토큰이 없거나 이미 사용되었으면 실패한다")
    void rejectsMissingOrPreviouslyConsumedRefreshToken() {
        when(refreshTokenPort.consume("used-refresh-token")).thenReturn(Optional.empty());

        assertAuthenticationError(
                () -> service.refresh("used-refresh-token"),
                AuthenticationErrorCode.INVALID_REFRESH_TOKEN
        );

        verify(accessTokenPort, never()).issue(any(), any());
    }

    @Test
    @DisplayName("리프레시 토큰이 비어 있으면 저장소를 조회하지 않고 실패한다")
    void rejectsBlankRefreshTokenWithoutAccessingStore() {
        assertAuthenticationError(
                () -> service.refresh(" "),
                AuthenticationErrorCode.INVALID_REFRESH_TOKEN
        );

        verify(refreshTokenPort, never()).consume(any());
    }

    @Test
    @DisplayName("만료된 리프레시 토큰이면 실패한다")
    void rejectsExpiredRefreshToken() {
        RefreshTokenSession expired = new RefreshTokenSession(1L, NOW);
        when(refreshTokenPort.consume("expired-refresh-token")).thenReturn(Optional.of(expired));

        assertAuthenticationError(
                () -> service.refresh("expired-refresh-token"),
                AuthenticationErrorCode.INVALID_REFRESH_TOKEN
        );
    }

    @Test
    @DisplayName("로그아웃하면 리프레시 토큰을 폐기한다")
    void revokesRefreshTokenOnLogout() {
        service.logout("refresh-token");

        verify(refreshTokenPort).revoke("refresh-token");
    }

    private Member persistedMember(Long id, String loginId, String passwordHash) {
        Member member = Member.register(
                loginId,
                "raw-password",
                returning(passwordHash),
                REGISTERED_AT
        );
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private void assertAuthenticationError(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
            AuthenticationErrorCode errorCode
    ) {
        assertThatThrownBy(callable)
                .isInstanceOf(DomainException.class)
                .extracting(exception -> ((DomainException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }
}
