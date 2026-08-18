package com.zoonza.coinalarm.member.internal.application.port.in;

import com.zoonza.coinalarm.member.internal.application.dto.MemberRegisterCommand;

public interface MemberCommandUseCase {
    void register(MemberRegisterCommand command);
}
