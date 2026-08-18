package com.zoonza.coinalarm.member.internal.application.service;

import com.zoonza.coinalarm.common.error.DomainException;
import com.zoonza.coinalarm.member.internal.application.dto.MemberRegisterCommand;
import com.zoonza.coinalarm.member.internal.application.port.in.MemberCommandUseCase;
import com.zoonza.coinalarm.member.internal.domain.Member;
import com.zoonza.coinalarm.member.internal.domain.MemberErrorCode;
import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import com.zoonza.coinalarm.member.internal.domain.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCommandService implements MemberCommandUseCase {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(MemberRegisterCommand command) {
        validateUniqueLoginId(command.loginId());

        Member member = Member.register(
                command.loginId(),
                command.rawPassword(),
                passwordEncoder,
                command.createdAt()
        );

        memberRepository.save(member);
    }

    private void validateUniqueLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new DomainException(MemberErrorCode.DUPLICATE_LOING_ID);
        }
    }
}
