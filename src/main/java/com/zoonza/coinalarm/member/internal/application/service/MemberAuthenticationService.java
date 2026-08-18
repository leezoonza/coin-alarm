package com.zoonza.coinalarm.member.internal.application.service;

import com.zoonza.coinalarm.common.error.DomainException;
import com.zoonza.coinalarm.member.internal.application.dto.AuthenticationTokens;
import com.zoonza.coinalarm.member.internal.application.dto.IssuedAccessToken;
import com.zoonza.coinalarm.member.internal.application.dto.RefreshTokenSession;
import com.zoonza.coinalarm.member.internal.application.port.in.MemberAuthenticationUseCase;
import com.zoonza.coinalarm.member.internal.application.port.out.AccessTokenPort;
import com.zoonza.coinalarm.member.internal.application.port.out.RefreshTokenPort;
import com.zoonza.coinalarm.member.internal.domain.AuthenticationErrorCode;
import com.zoonza.coinalarm.member.internal.domain.Member;
import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import com.zoonza.coinalarm.member.internal.domain.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MemberAuthenticationService implements MemberAuthenticationUseCase {
    private final Clock clock;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public AuthenticationTokens login(String loginId, String rawPassword) {
        Member member = memberRepository.findByLoginId(loginId)
                .filter(found -> passwordEncoder.matches(rawPassword, found.getPasswordHash()))
                .orElseThrow(() -> new DomainException(AuthenticationErrorCode.INVALID_CREDENTIALS));

        Instant loggedInAt = Instant.now(clock);

        member.recordLogin(loggedInAt);

        return issueTokens(member.getId(), loggedInAt);
    }

    @Override
    public AuthenticationTokens refresh(String refreshToken) {
        requireRefreshToken(refreshToken);

        Instant now = Instant.now(clock);

        RefreshTokenSession session = refreshTokenPort.consume(refreshToken)
                .filter(found -> found.expiresAt().isAfter(now))
                .orElseThrow(() -> new DomainException(AuthenticationErrorCode.INVALID_REFRESH_TOKEN));

        return issueTokens(session.memberId(), now);
    }

    @Override
    public void logout(String refreshToken) {
        requireRefreshToken(refreshToken);

        refreshTokenPort.revoke(refreshToken);
    }

    private AuthenticationTokens issueTokens(Long memberId, Instant issuedAt) {
        IssuedAccessToken accessToken = accessTokenPort.issue(memberId, issuedAt);

        String refreshToken = refreshTokenPort.issue(memberId, issuedAt);
        long expiresIn = Duration.between(issuedAt, accessToken.expiresAt()).toSeconds();

        return AuthenticationTokens.bearer(accessToken.value(), refreshToken, expiresIn);
    }

    private void requireRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new DomainException(AuthenticationErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
