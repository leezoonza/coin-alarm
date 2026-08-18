package com.zoonza.coinalarm.member.internal.application.service;

import com.zoonza.coinalarm.member.internal.application.port.in.MemberQueryUseCase;
import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryService implements MemberQueryUseCase {
    private final MemberRepository memberRepository;

    @Override
    public boolean isLoginIdAvailable(String loginId) {
        return !memberRepository.existsByLoginId(loginId);
    }
}
