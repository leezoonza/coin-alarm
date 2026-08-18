package com.zoonza.coinalarm.member.internal.adapter.in;

import com.zoonza.coinalarm.TestcontainersConfiguration;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.LoginRequest;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.MemberRegisterRequest;
import com.zoonza.coinalarm.member.internal.adapter.out.persistence.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthenticationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("JWT로 인증하고 불투명 리프레시 토큰을 교체한 뒤 폐기한다")
    void authenticatesWithJwtAndRotatesThenRevokesOpaqueRefreshTokens() throws Exception {
        registerMember();

        Tokens loggedIn = login();

        entityManager.flush();
        entityManager.clear();

        assertThat(memberRepository.findByLoginId("zoonza"))
                .get()
                .extracting(member -> member.getLastLoginAt())
                .isNotNull();

        mockMvc.perform(get("/actuator"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator")
                        .header("Authorization", "Bearer " + loggedIn.accessToken()))
                .andExpect(status().isOk());

        Tokens refreshed = refresh(loggedIn.refreshTokenCookie());
        assertThat(refreshed.accessToken()).isNotEqualTo(loggedIn.accessToken());
        assertThat(refreshed.refreshTokenCookie().getValue())
                .isNotEqualTo(loggedIn.refreshTokenCookie().getValue());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(loggedIn.refreshTokenCookie()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.code").value("AUTH-002"));

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshed.refreshTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(RefreshTokenCookieManager.COOKIE_NAME, 0));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshed.refreshTokenCookie()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.code").value("AUTH-002"));
    }

    private void registerMember() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MemberRegisterRequest("zoonza", "secret")
                        )))
                .andExpect(status().isCreated());
    }

    private Tokens login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("zoonza", "secret")
                        )))
                .andExpect(status().isOk())
                .andReturn();

        return readTokens(result);
    }

    private Tokens refresh(Cookie refreshTokenCookie) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        return readTokens(result);
    }

    private Tokens readTokens(MvcResult result) throws Exception {
        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        return new Tokens(
                data.get("accessToken").asText(),
                result.getResponse().getCookie(RefreshTokenCookieManager.COOKIE_NAME)
        );
    }

    private record Tokens(String accessToken, Cookie refreshTokenCookie) {
    }
}
