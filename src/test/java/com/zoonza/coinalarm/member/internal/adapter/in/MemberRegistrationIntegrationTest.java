package com.zoonza.coinalarm.member.internal.adapter.in;

import com.zoonza.coinalarm.BinanceStreamService;
import com.zoonza.coinalarm.TestcontainersConfiguration;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.MemberRegisterRequest;
import com.zoonza.coinalarm.member.internal.adapter.out.persistence.MemberJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
class MemberRegistrationIntegrationTest {

    @MockitoBean
    private BinanceStreamService binanceStreamService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Test
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
                });
    }
}
