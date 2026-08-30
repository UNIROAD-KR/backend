package com.uniroad.backend.global.config;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.MemberStatus;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Local-profile-only development account bootstrap. */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDevelopmentDataInitializer implements ApplicationRunner {

    public static final String USERNAME = "localtest";
    public static final String PASSWORD = "Local1234";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * application-local.yml의 local.development.test-account.role 을 따른다.
     * 기존에는 이 설정을 읽지 않고 USER로 고정돼 있어, 로컬에서 관리자 기능을 검증할 수 없었다.
     */
    @Value("${local.development.test-account.role:USER}")
    private String testAccountRole;

    @Override
    public void run(ApplicationArguments args) {
        Role role = resolveRole();

        memberRepository.findByUsername(USERNAME).ifPresentOrElse(
                member -> {
                    // Keep the documented password valid for an existing local database.
                    member.updatePassword(passwordEncoder.encode(PASSWORD));
                    member.updateRole(role);
                    memberRepository.save(member);
                    log.info("[Local] Development account ready: username={}, role={}", USERNAME, role);
                },
                () -> {
                    memberRepository.save(Member.builder()
                            .username(USERNAME)
                            .email("localtest@uniroad.local")
                            .password(passwordEncoder.encode(PASSWORD))
                            .name("Local Test User")
                            .provider("LOCAL")
                            .role(role)
                            .status(MemberStatus.ACTIVE)
                            .build());
                    log.info("[Local] Development account created: username={}, role={}", USERNAME, role);
                });
    }

    private Role resolveRole() {
        try {
            return Role.valueOf(testAccountRole.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("[Local] Unknown test-account role '{}', falling back to USER", testAccountRole);
            return Role.USER;
        }
    }
}
