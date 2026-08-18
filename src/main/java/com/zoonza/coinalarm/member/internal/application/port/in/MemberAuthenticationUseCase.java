package com.zoonza.coinalarm.member.internal.application.port.in;

import com.zoonza.coinalarm.member.internal.application.dto.AuthenticationTokens;

public interface MemberAuthenticationUseCase {
    AuthenticationTokens login(String loginId, String rawPassword);

    AuthenticationTokens refresh(String refreshToken);

    void logout(String refreshToken);
}
