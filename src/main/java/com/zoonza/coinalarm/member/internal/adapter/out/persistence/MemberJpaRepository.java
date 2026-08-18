package com.zoonza.coinalarm.member.internal.adapter.out.persistence;

import com.zoonza.coinalarm.member.internal.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    boolean existsByLoginId(String loginId);
}
