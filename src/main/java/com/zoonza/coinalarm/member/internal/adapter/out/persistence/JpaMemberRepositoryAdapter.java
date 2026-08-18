package com.zoonza.coinalarm.member.internal.adapter.out.persistence;

import com.zoonza.coinalarm.member.internal.domain.Member;
import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaMemberRepositoryAdapter implements MemberRepository {
    private final MemberJpaRepository repository;

    @Override
    public boolean existsByLoginId(String loginId) {
        return repository.existsByLoginId(loginId);
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        return repository.findByLoginId(loginId);
    }

    @Override
    public Member save(Member member) {
        return repository.save(member);
    }
}
