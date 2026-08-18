package com.zoonza.coinalarm.member.internal.domain;

public interface PasswordEncoder {
    String encode(String rawPassword);
}
