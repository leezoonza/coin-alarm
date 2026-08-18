package com.zoonza.coinalarm.member.internal.domain;

public interface PasswordEncoder {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
