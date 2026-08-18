package com.zoonza.coinalarm.member.internal.adapter.out.persistence;

import com.zoonza.coinalarm.member.internal.domain.Member;
import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaMemberRepositoryAdapter implements MemberRepository {
    private final MemberJpaRepository repository;

    @Override
    public boolean existsByLoginId(String loginId) {
        return repository.existsByLoginId(loginId);
    }

    @Override
    public Member save(Member member) {
        return repository.save(member);
    }
}
