package com.zoonza.coinalarm.member.internal.adapter.in;

import com.zoonza.coinalarm.common.response.ApiResponse;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.AccessTokenResponse;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.LoginRequest;
import com.zoonza.coinalarm.member.internal.application.dto.AuthenticationTokens;
import com.zoonza.coinalarm.member.internal.application.port.in.MemberAuthenticationUseCase;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final MemberAuthenticationUseCase memberAuthenticationUseCase;
    private final RefreshTokenCookieManager refreshTokenCookieManager;

    @PostMapping("/login")
    public ApiResponse<AccessTokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthenticationTokens tokens = memberAuthenticationUseCase.login(
                request.loginId(),
                request.password()
        );

        return respondWithTokens(tokens, response);
    }

    @PostMapping("/refresh")
    public ApiResponse<AccessTokenResponse> refresh(
            @CookieValue(name = RefreshTokenCookieManager.COOKIE_NAME, required = false)
            String refreshToken,
            HttpServletResponse response
    ) {
        AuthenticationTokens tokens = memberAuthenticationUseCase.refresh(refreshToken);

        return respondWithTokens(tokens, response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = RefreshTokenCookieManager.COOKIE_NAME, required = false)
            String refreshToken,
            HttpServletResponse response
    ) {
        memberAuthenticationUseCase.logout(refreshToken);

        refreshTokenCookieManager.clear(response);

        return ApiResponse.success();
    }

    private ApiResponse<AccessTokenResponse> respondWithTokens(
            AuthenticationTokens tokens,
            HttpServletResponse response
    ) {
        refreshTokenCookieManager.set(response, tokens.refreshToken());

        return ApiResponse.success(AccessTokenResponse.from(tokens));
    }
}
