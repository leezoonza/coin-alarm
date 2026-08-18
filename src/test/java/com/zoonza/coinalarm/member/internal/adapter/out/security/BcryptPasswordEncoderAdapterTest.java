package com.zoonza.coinalarm.member.internal.adapter.out.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordEncoderAdapterTest {

    @Test
    @DisplayName("비밀번호를 BCrypt로 암호화한다")
    void encodesPasswordWithBcrypt() {
        BcryptPasswordEncoderAdapter adapter = new BcryptPasswordEncoderAdapter();

        String passwordHash = adapter.encode("secret");

        assertThat(passwordHash).isNotEqualTo("secret");
        assertThat(new BCryptPasswordEncoder().matches("secret", passwordHash)).isTrue();
    }

    @Test
    @DisplayName("원문 비밀번호와 암호화된 비밀번호의 일치 여부를 확인한다")
    void matchesRawPasswordAgainstHash() {
        BcryptPasswordEncoderAdapter adapter = new BcryptPasswordEncoderAdapter();
        String passwordHash = new BCryptPasswordEncoder().encode("secret");

        assertThat(adapter.matches("secret", passwordHash)).isTrue();
        assertThat(adapter.matches("wrong", passwordHash)).isFalse();
    }
}
