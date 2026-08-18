package com.zoonza.coinalarm.member.internal.adapter.in;

import com.zoonza.coinalarm.member.internal.adapter.out.security.AuthenticationProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieManager {
    public static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/auth";
    private static final String SAME_SITE = "Lax";

    private final AuthenticationProperties properties;

    public void set(HttpServletResponse response, String refreshToken) {
        add(response, refreshToken, properties.refreshTokenTtl());
    }

    public void clear(HttpServletResponse response) {
        add(response, "", Duration.ZERO);
    }

    private void add(
            HttpServletResponse response,
            String value,
            Duration maxAge
    ) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(properties.refreshTokenCookieSecure())
                .sameSite(SAME_SITE)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
