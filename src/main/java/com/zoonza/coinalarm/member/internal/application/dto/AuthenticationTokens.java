package com.zoonza.coinalarm.member.internal.application.dto;

public record AuthenticationTokens(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public static AuthenticationTokens bearer(
            String accessToken,
            String refreshToken,
            long expiresIn
    ) {
        return new AuthenticationTokens(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
