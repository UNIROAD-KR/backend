package com.uniroad.backend.domain.member.repository;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@org.springframework.context.annotation.Import(com.uniroad.backend.global.config.JpaConfig.class)
@ActiveProfiles("test") // H2 설정을 사용하기 위해 test 프로필 활성화
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 회원 조회")
    void findByEmail_Success() {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .password("password")
                .name("테스터")
                .role(Role.USER)
                .provider("LOCAL")
                .build();
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByEmail("test@test.com");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("Provider와 ProviderId로 회원 조회")
    void findByProviderAndProviderId_Success() {
        // given
        Member member = Member.builder()
                .email("kakao@test.com")
                .password("password")
                .name("카카오사용자")
                .role(Role.USER)
                .provider("KAKAO")
                .providerId("123456")
                .build();
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByProviderAndProviderId("KAKAO", "123456");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getProvider()).isEqualTo("KAKAO");
        assertThat(foundMember.get().getProviderId()).isEqualTo("123456");
    }
}
