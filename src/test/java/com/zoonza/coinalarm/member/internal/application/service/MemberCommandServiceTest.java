package com.zoonza.coinalarm.member.internal.application.service;

import com.zoonza.coinalarm.common.error.DomainException;
import com.zoonza.coinalarm.member.internal.application.dto.MemberRegisterCommand;
import com.zoonza.coinalarm.member.internal.domain.Member;
import com.zoonza.coinalarm.member.internal.domain.MemberErrorCode;
import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import com.zoonza.coinalarm.member.internal.domain.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {
    private static final Instant CREATED_AT = Instant.parse("2026-08-18T00:00:00Z");

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MemberCommandService service;

    @BeforeEach
    void setUp() {
        service = new MemberCommandService(memberRepository, passwordEncoder);
    }

    @Test
    void registersMemberWhenLoginIdIsAvailable() {
        MemberRegisterCommand command = new MemberRegisterCommand(
                "zoonza",
                "secret",
                CREATED_AT
        );
        when(memberRepository.existsByLoginId("zoonza")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        service.register(command);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getLoginId()).isEqualTo("zoonza");
        assertThat(savedMember.getPasswordHash()).isEqualTo("encoded-secret");
        assertThat(savedMember.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(savedMember.getUpdatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void rejectsDuplicateLoginIdWithoutEncodingOrSaving() {
        MemberRegisterCommand command = new MemberRegisterCommand(
                "zoonza",
                "secret",
                CREATED_AT
        );
        when(memberRepository.existsByLoginId("zoonza")).thenReturn(true);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(DomainException.class)
                .extracting(exception -> ((DomainException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.DUPLICATE_LOING_ID);

        verify(passwordEncoder, never()).encode("secret");
        verify(memberRepository, never()).save(org.mockito.ArgumentMatchers.any(Member.class));
    }
}
