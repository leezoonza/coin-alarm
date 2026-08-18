package com.zoonza.coinalarm.member.internal.adapter.in;

import com.zoonza.coinalarm.common.error.DomainException;
import com.zoonza.coinalarm.global.exception.GlobalExceptionHandler;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.MemberRegisterRequest;
import com.zoonza.coinalarm.member.internal.application.dto.MemberRegisterCommand;
import com.zoonza.coinalarm.member.internal.application.port.in.MemberCommandUseCase;
import com.zoonza.coinalarm.member.internal.application.port.in.MemberQueryUseCase;
import com.zoonza.coinalarm.member.internal.domain.MemberErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {
    private static final Instant NOW = Instant.parse("2026-08-18T00:00:00Z");

    @Mock
    private MemberQueryUseCase memberQueryUseCase;

    @Mock
    private MemberCommandUseCase memberCommandUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        MemberController controller = new MemberController(
                clock,
                memberQueryUseCase,
                memberCommandUseCase
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("로그인 아이디의 사용 가능 여부를 응답한다")
    void returnsLoginIdAvailability() throws Exception {
        when(memberQueryUseCase.isLoginIdAvailable("available-id")).thenReturn(true);

        mockMvc.perform(get("/api/members/login-id/availability")
                        .param("loginId", "available-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("현재 시각을 기준으로 회원을 등록한다")
    void registersMemberWithCurrentTime() throws Exception {
        MemberRegisterRequest request = new MemberRegisterRequest(
                "zoonza",
                "secret"
        );

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<MemberRegisterCommand> commandCaptor = ArgumentCaptor.forClass(MemberRegisterCommand.class);

        verify(memberCommandUseCase).register(commandCaptor.capture());

        assertThat(commandCaptor.getValue()).isEqualTo(new MemberRegisterCommand(
                "zoonza",
                "secret",
                NOW
        ));
    }

    @Test
    @DisplayName("회원가입 요청 값이 비어 있으면 검증 오류를 응답한다")
    void returnsValidationErrorsForBlankRegistrationFields() throws Exception {
        MemberRegisterRequest request = new MemberRegisterRequest(
                " ",
                ""
        );

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("COMMON-002"))
                .andExpect(jsonPath("$.data.errors", hasSize(2)));
    }

    @Test
    @DisplayName("도메인 예외를 오류 응답으로 변환한다")
    void convertsDomainExceptionToErrorResponse() throws Exception {
        when(memberQueryUseCase.isLoginIdAvailable("used-id"))
                .thenThrow(new DomainException(MemberErrorCode.DUPLICATE_LOING_ID));

        mockMvc.perform(get("/api/members/login-id/availability")
                        .param("loginId", "used-id"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("MEMBER-001"))
                .andExpect(jsonPath("$.data.message")
                        .value("이미 사용 중인 로그인 아이디입니다."));
    }

    @Test
    @DisplayName("예상하지 못한 예외의 상세 정보를 응답에 노출하지 않는다")
    void hidesUnexpectedExceptionDetails() throws Exception {
        when(memberQueryUseCase.isLoginIdAvailable("broken-id"))
                .thenThrow(new IllegalStateException("database password leaked"));

        mockMvc.perform(get("/api/members/login-id/availability")
                        .param("loginId", "broken-id"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("COMMON-001"))
                .andExpect(jsonPath("$.data.message").value("서버 내부 오류가 발생했습니다."));
    }
}
