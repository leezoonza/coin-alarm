package com.zoonza.coinalarm.member.internal.application.service;

import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberQueryService service;

    @BeforeEach
    void setUp() {
        service = new MemberQueryService(memberRepository);
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 로그인 아이디를 사용할 수 있다")
    void loginIdIsAvailableWhenMemberDoesNotExist() {
        when(memberRepository.existsByLoginId("available-id")).thenReturn(false);

        boolean available = service.isLoginIdAvailable("available-id");

        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("회원이 존재하면 로그인 아이디를 사용할 수 없다")
    void loginIdIsUnavailableWhenMemberExists() {
        when(memberRepository.existsByLoginId("used-id")).thenReturn(true);

        boolean available = service.isLoginIdAvailable("used-id");

        assertThat(available).isFalse();
    }
}
