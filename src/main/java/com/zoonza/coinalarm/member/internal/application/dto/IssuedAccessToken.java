package com.zoonza.coinalarm.member.internal.application.dto;

import java.time.Instant;

public record IssuedAccessToken(
        String value,
        Instant expiresAt
) {
}
