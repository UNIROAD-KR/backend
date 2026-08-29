package com.uniroad.backend.global.config;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.MemberStatus;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void run(ApplicationArguments args) {
        memberRepository.findByUsername(USERNAME).ifPresentOrElse(
                member -> {
                    // Keep the documented password valid for an existing local database.
                    member.updatePassword(passwordEncoder.encode(PASSWORD));
                    memberRepository.save(member);
                    log.info("[Local] Development account ready: username={}", USERNAME);
                },
                () -> {
                    memberRepository.save(Member.builder()
                            .username(USERNAME)
                            .email("localtest@uniroad.local")
                            .password(passwordEncoder.encode(PASSWORD))
                            .name("Local Test User")
                            .provider("LOCAL")
                            .role(Role.USER)
                            .status(MemberStatus.ACTIVE)
                            .build());
                    log.info("[Local] Development account created: username={}", USERNAME);
                });
    }
}
