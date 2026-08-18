package com.zoonza.coinalarm.member.fixture;

import com.zoonza.coinalarm.member.internal.domain.PasswordEncoder;

public final class PasswordEncoderFixture {
    private PasswordEncoderFixture() {
    }

    public static PasswordEncoder returning(String passwordHash) {
        return new PasswordEncoder() {
            @Override
            public String encode(String rawPassword) {
                return passwordHash;
            }

            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return passwordHash.equals(encodedPassword);
            }
        };
    }
}
