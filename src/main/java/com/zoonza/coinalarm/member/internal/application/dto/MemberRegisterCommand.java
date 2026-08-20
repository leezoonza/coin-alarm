package com.zoonza.coinalarm.member.internal.application.dto;

public record MemberRegisterCommand(
        String loginId,
        String rawPassword
) {
}
