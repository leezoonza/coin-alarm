package com.zoonza.coinalarm.member.fixture;

import com.zoonza.coinalarm.member.internal.domain.Member;

import java.time.Instant;

import static com.zoonza.coinalarm.member.fixture.PasswordEncoderFixture.returning;

public final class MemberFixture {
    public static final String PASSWORD_HASH = "encoded-password";
    public static final Instant CREATED_AT = Instant.parse("2026-08-18T00:00:00Z");

    private MemberFixture() {
    }

    public static Member member(String loginId) {
        return Member.register(
                loginId,
                "raw-password",
                returning(PASSWORD_HASH),
                CREATED_AT
        );
    }
}
