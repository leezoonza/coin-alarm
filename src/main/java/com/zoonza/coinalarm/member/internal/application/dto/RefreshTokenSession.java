package com.zoonza.coinalarm.member.internal.application.dto;

import java.time.Instant;

public record RefreshTokenSession(
        Long memberId,
        Instant expiresAt
) {
}
