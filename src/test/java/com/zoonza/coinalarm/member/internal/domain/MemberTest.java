package com.zoonza.coinalarm.member.internal.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MemberTest {
    private static final Instant CREATED_AT = Instant.parse("2026-08-18T00:00:00Z");

    @Test
    void registersWithEncodedPasswordAndCreationTime() {
        PasswordEncoder passwordEncoder = rawPassword -> "encoded-" + rawPassword;

        Member member = Member.register(
                "zoonza",
                "secret",
                passwordEncoder,
                CREATED_AT
        );

        assertThat(member.getLoginId()).isEqualTo("zoonza");
        assertThat(member.getPasswordHash()).isEqualTo("encoded-secret");
        assertThat(member.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(member.getUpdatedAt()).isEqualTo(CREATED_AT);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankLoginId(String loginId) {
        assertThatIllegalArgumentException().isThrownBy(() -> Member.register(
                loginId,
                "secret",
                ignored -> "encoded-secret",
                CREATED_AT
        ));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankEncodedPassword(String passwordHash) {
        assertThatIllegalArgumentException().isThrownBy(() -> Member.register(
                "zoonza",
                "secret",
                ignored -> passwordHash,
                CREATED_AT
        ));
    }

    @Test
    void rejectsNullCreationTime() {
        assertThatIllegalArgumentException().isThrownBy(() -> Member.register(
                "zoonza",
                "secret",
                ignored -> "encoded-secret",
                null
        ));
    }
}
