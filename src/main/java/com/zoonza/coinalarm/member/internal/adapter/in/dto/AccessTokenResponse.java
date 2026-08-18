package com.zoonza.coinalarm.member.internal.adapter.in.dto;

import com.zoonza.coinalarm.member.internal.application.dto.AuthenticationTokens;

public record AccessTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static AccessTokenResponse from(AuthenticationTokens tokens) {
        return new AccessTokenResponse(
                tokens.accessToken(),
                tokens.tokenType(),
                tokens.expiresIn()
        );
    }
}
