package com.zoonza.coinalarm.member.internal.domain;

public interface MemberRepository {
    boolean existsByLoginId(String loginId);

    Member save(Member member);
}
