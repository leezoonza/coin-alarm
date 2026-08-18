package com.zoonza.coinalarm.member.internal.domain;

import java.util.Optional;

public interface MemberRepository {
    boolean existsByLoginId(String loginId);

    Optional<Member> findByLoginId(String loginId);

    Member save(Member member);
}
