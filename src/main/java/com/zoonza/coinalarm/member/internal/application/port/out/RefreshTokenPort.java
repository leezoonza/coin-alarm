package com.zoonza.coinalarm.member.internal.application.port.out;

import com.zoonza.coinalarm.member.internal.application.dto.RefreshTokenSession;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenPort {
    String issue(Long memberId, Instant issuedAt);

    Optional<RefreshTokenSession> consume(String refreshToken);

    void revoke(String refreshToken);
}
