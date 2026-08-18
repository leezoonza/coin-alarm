package com.zoonza.coinalarm.member.internal.adapter.out.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordEncoderAdapterTest {

    @Test
    void encodesPasswordWithBcrypt() {
        BcryptPasswordEncoderAdapter adapter = new BcryptPasswordEncoderAdapter();

        String passwordHash = adapter.encode("secret");

        assertThat(passwordHash).isNotEqualTo("secret");
        assertThat(new BCryptPasswordEncoder().matches("secret", passwordHash)).isTrue();
    }
}
