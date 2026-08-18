package com.zoonza.coinalarm.member.internal.application.port.in;

public interface MemberQueryUseCase {
    boolean isLoginIdAvailable(String loginId);
}
