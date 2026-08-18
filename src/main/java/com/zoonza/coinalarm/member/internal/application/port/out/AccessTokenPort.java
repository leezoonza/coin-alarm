package com.zoonza.coinalarm.member.internal.application.port.out;

import com.zoonza.coinalarm.member.internal.application.dto.IssuedAccessToken;

import java.time.Instant;

public interface AccessTokenPort {
    IssuedAccessToken issue(Long memberId, Instant issuedAt);
}
