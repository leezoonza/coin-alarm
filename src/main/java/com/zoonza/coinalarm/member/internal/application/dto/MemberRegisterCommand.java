package com.zoonza.coinalarm.member.internal.application.dto;

import java.time.Instant;

public record MemberRegisterCommand(
        String loginId,
        String rawPassword,
        Instant createdAt
) {
}
