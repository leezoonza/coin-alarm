package com.zoonza.coinalarm.member.internal.adapter.in.dto;

import com.zoonza.coinalarm.member.internal.application.dto.MemberRegisterCommand;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record MemberRegisterRequest(
        @NotBlank(message = "로그인 아이디를 입력해 주세요.")
        String loginId,

        @NotBlank(message = "비밀번호을 입력해 주세요.")
        String rawPassword
) {
    public MemberRegisterCommand toCommand(Instant createdAt) {
        return new MemberRegisterCommand(
                this.loginId,
                this.rawPassword,
                createdAt
        );
    }
}
