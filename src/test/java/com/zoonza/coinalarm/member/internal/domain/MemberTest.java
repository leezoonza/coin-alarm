package com.zoonza.coinalarm.member.internal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static com.zoonza.coinalarm.member.fixture.PasswordEncoderFixture.returning;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MemberTest {
    private static final Instant CREATED_AT = Instant.parse("2026-08-18T00:00:00Z");

    @Test
    @DisplayName("암호화된 비밀번호와 생성 시각으로 회원을 등록한다")
    void registersWithEncodedPasswordAndCreationTime() {
        PasswordEncoder passwordEncoder = returning("encoded-secret");

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
        assertThat(member.getLastLoginAt()).isNull();
    }

    @Test
    @DisplayName("마지막 로그인 시각을 기록한다")
    void recordsLastLoginTime() {
        Member member = Member.register(
                "zoonza",
                "secret",
                returning("encoded-secret"),
                CREATED_AT
        );
        Instant loggedInAt = CREATED_AT.plusSeconds(60);

        member.recordLogin(loggedInAt);

        assertThat(member.getLastLoginAt()).isEqualTo(loggedInAt);
        assertThat(member.getUpdatedAt()).isEqualTo(loggedInAt);
    }

    @Test
    @DisplayName("마지막 로그인 시각이 null이면 예외가 발생한다")
    void rejectsNullLastLoginTime() {
        Member member = Member.register(
                "zoonza",
                "secret",
                returning("encoded-secret"),
                CREATED_AT
        );

        assertThatIllegalArgumentException().isThrownBy(() -> member.recordLogin(null));
    }

    @ParameterizedTest
    @DisplayName("로그인 아이디가 비어 있으면 예외가 발생한다")
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankLoginId(String loginId) {
        assertThatIllegalArgumentException().isThrownBy(() -> Member.register(
                loginId,
                "secret",
                returning("encoded-secret"),
                CREATED_AT
        ));
    }

    @ParameterizedTest
    @DisplayName("암호화된 비밀번호가 비어 있으면 예외가 발생한다")
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankEncodedPassword(String passwordHash) {
        assertThatIllegalArgumentException().isThrownBy(() -> Member.register(
                "zoonza",
                "secret",
                returning(passwordHash),
                CREATED_AT
        ));
    }

    @Test
    @DisplayName("생성 시각이 null이면 예외가 발생한다")
    void rejectsNullCreationTime() {
        assertThatIllegalArgumentException().isThrownBy(() -> Member.register(
                "zoonza",
                "secret",
                returning("encoded-secret"),
                null
        ));
    }
}
