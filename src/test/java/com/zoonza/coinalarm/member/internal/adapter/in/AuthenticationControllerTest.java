package com.zoonza.coinalarm.member.internal.adapter.in;

import com.zoonza.coinalarm.common.error.DomainException;
import com.zoonza.coinalarm.global.exception.GlobalExceptionHandler;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.LoginRequest;
import com.zoonza.coinalarm.member.internal.adapter.out.security.AuthenticationProperties;
import com.zoonza.coinalarm.member.internal.application.dto.AuthenticationTokens;
import com.zoonza.coinalarm.member.internal.application.port.in.MemberAuthenticationUseCase;
import com.zoonza.coinalarm.member.internal.domain.AuthenticationErrorCode;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock
    private MemberAuthenticationUseCase memberAuthenticationUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthenticationProperties properties = new AuthenticationProperties(
                "coin-alarm",
                Duration.ofMinutes(10),
                Duration.ofDays(7),
                "test-secret-key-that-is-at-least-32-bytes",
                100,
                false
        );
        RefreshTokenCookieManager cookieManager = new RefreshTokenCookieManager(properties);
        AuthenticationController controller = new AuthenticationController(
                memberAuthenticationUseCase,
                cookieManager
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("로그인하면 액세스 토큰을 응답하고 리프레시 토큰을 쿠키에 저장한다")
    void logsInAndReturnsAccessTokenWithRefreshTokenCookie() throws Exception {
        LoginRequest request = new LoginRequest("zoonza", "secret");
        when(memberAuthenticationUseCase.login("zoonza", "secret"))
                .thenReturn(tokens("access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(600))
                .andExpect(cookie().value(RefreshTokenCookieManager.COOKIE_NAME, "refresh-token"))
                .andExpect(cookie().httpOnly(RefreshTokenCookieManager.COOKIE_NAME, true))
                .andExpect(cookie().path(RefreshTokenCookieManager.COOKIE_NAME, "/api/auth"))
                .andExpect(cookie().maxAge(RefreshTokenCookieManager.COOKIE_NAME, 604800));
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키로 토큰을 재발급한다")
    void refreshesTokens() throws Exception {
        when(memberAuthenticationUseCase.refresh("old-refresh-token"))
                .thenReturn(tokens("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(
                                RefreshTokenCookieManager.COOKIE_NAME,
                                "old-refresh-token"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(cookie().value(
                        RefreshTokenCookieManager.COOKIE_NAME,
                        "new-refresh-token"
                ));
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키가 없으면 인증에 실패한다")
    void rejectsRefreshWithoutCookie() throws Exception {
        when(memberAuthenticationUseCase.refresh(null))
                .thenThrow(new DomainException(AuthenticationErrorCode.INVALID_REFRESH_TOKEN));

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.code").value("AUTH-002"));
    }

    @Test
    @DisplayName("로그아웃하면 리프레시 토큰을 폐기하고 쿠키를 삭제한다")
    void logsOutByRevokingRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie(
                                RefreshTokenCookieManager.COOKIE_NAME,
                                "refresh-token"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(cookie().maxAge(RefreshTokenCookieManager.COOKIE_NAME, 0));

        verify(memberAuthenticationUseCase).logout("refresh-token");
    }

    @Test
    @DisplayName("로그인 요청 값이 비어 있으면 검증에 실패한다")
    void rejectsBlankLoginRequest() throws Exception {
        LoginRequest request = new LoginRequest(" ", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("COMMON-002"))
                .andExpect(jsonPath("$.data.errors.length()").value(2));
    }

    @Test
    @DisplayName("로그인 정보가 올바르지 않으면 인증에 실패한다")
    void returnsUnauthorizedForInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("zoonza", "wrong");
        when(memberAuthenticationUseCase.login("zoonza", "wrong"))
                .thenThrow(new DomainException(AuthenticationErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("AUTH-001"));
    }

    private AuthenticationTokens tokens(String accessToken, String refreshToken) {
        return new AuthenticationTokens(accessToken, refreshToken, "Bearer", 600);
    }
}
