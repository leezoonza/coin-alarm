package com.zoonza.coinalarm.member.internal.adapter.in;

import com.zoonza.coinalarm.TestcontainersConfiguration;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.MemberRegisterRequest;
import com.zoonza.coinalarm.member.internal.adapter.out.persistence.MemberJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
class MemberRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Test
    @DisplayName("회원을 등록하고 데이터베이스에 저장한다")
    void registersAndPersistsMember() throws Exception {
        MemberRegisterRequest request = new MemberRegisterRequest("zoonza", "secret");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(memberRepository.findAll())
                .singleElement()
                .satisfies(member -> {
                    assertThat(member.getLoginId()).isEqualTo("zoonza");
                    assertThat(member.getPasswordHash()).isNotEqualTo("secret");
                    assertThat(new BCryptPasswordEncoder()
                            .matches("secret", member.getPasswordHash()))
                            .isTrue();
                    assertThat(member.getCreatedAt()).isNotNull();
                    assertThat(member.getUpdatedAt()).isEqualTo(member.getCreatedAt());
                    assertThat(member.getLastLoginAt()).isNull();
                });
    }
}
