package com.zoonza.coinalarm.member.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Member(
            String loginId,
            String passwordHash,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (!StringUtils.hasText(loginId)) {
            throw new IllegalArgumentException("로그인 아이디는 빈 값일 수 없습니다.");
        }

        if (!StringUtils.hasText(passwordHash)) {
            throw new IllegalArgumentException("비밀번호 해시는 빈 값일 수 없습니다.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("생성 일시는 null일 수 업습니다.");
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException("수정 일시는 null일 수 업습니다.");
        }

        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Member register(
            String loginId,
            String rawPassword,
            PasswordEncoder passwordEncoder,
            Instant createdAt
    ) {
        return new Member(
                loginId,
                passwordEncoder.encode(rawPassword),
                createdAt,
                createdAt
        );
    }
}
