package com.zoonza.coinalarm.member.internal.adapter.out.persistence;

import com.zoonza.coinalarm.TestcontainersConfiguration;
import com.zoonza.coinalarm.member.internal.domain.Member;
import com.zoonza.coinalarm.member.internal.domain.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static com.zoonza.coinalarm.member.fixture.MemberFixture.member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = NONE)
@Import({
        TestcontainersConfiguration.class,
        JpaMemberRepositoryAdapter.class
})
class JpaMemberRepositoryAdapterIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void savesMemberAndFindsItByLoginId() {
        String loginId = "stored-member";
        assertThat(memberRepository.existsByLoginId(loginId)).isFalse();

        Member savedMember = memberRepository.save(member(loginId));

        assertThat(savedMember.getId()).isNotNull();
        assertThat(memberRepository.existsByLoginId(loginId)).isTrue();
    }
}
