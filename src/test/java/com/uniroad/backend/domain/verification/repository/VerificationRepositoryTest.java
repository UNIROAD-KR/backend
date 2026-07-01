package com.uniroad.backend.domain.verification.repository;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.verification.entity.Verification;
import com.uniroad.backend.domain.verification.entity.VerificationStatus;
import com.uniroad.backend.global.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class VerificationRepositoryTest {

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("findByMemberAndIsCurrentTrue returns current verification")
    void findByMemberAndIsCurrentTrue_Success() {
        // given
        Member member = memberRepository.save(member("current@test.com"));
        verificationRepository.save(verification(member, "old-image", VerificationStatus.REJECTED, false));
        Verification current = verificationRepository.save(verification(member, "new-image", VerificationStatus.PENDING, true));

        // when
        Optional<Verification> found = verificationRepository.findByMemberAndIsCurrentTrue(member);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(current.getId());
        assertThat(found.get().getImageUrl()).isEqualTo("new-image");
    }

    @Test
    @DisplayName("findAllByStatusAndIsCurrentTrue returns only current pending verifications")
    void findAllByStatusAndIsCurrentTrue_Success() {
        // given
        Member pendingMember = memberRepository.save(member("pending@test.com"));
        Member oldMember = memberRepository.save(member("old@test.com"));
        Member approvedMember = memberRepository.save(member("approved@test.com"));

        Verification pendingCurrent = verificationRepository.save(
                verification(pendingMember, "pending-image", VerificationStatus.PENDING, true)
        );
        verificationRepository.save(verification(oldMember, "old-image", VerificationStatus.PENDING, false));
        verificationRepository.save(verification(approvedMember, "approved-image", VerificationStatus.APPROVED, true));

        // when
        List<Verification> found = verificationRepository.findAllByStatusAndIsCurrentTrue(VerificationStatus.PENDING);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(pendingCurrent.getId());
        assertThat(found.get(0).getMember().getEmail()).isEqualTo("pending@test.com");
    }

    @Test
    @DisplayName("findAllByMemberIdOrderBySubmittedAtDesc returns member verifications in latest order")
    void findAllByMemberIdOrderBySubmittedAtDesc_Success() {
        // given
        Member member = memberRepository.save(member("history@test.com"));
        Member otherMember = memberRepository.save(member("other@test.com"));

        Verification old = verificationRepository.save(verification(
                member,
                "old-image",
                VerificationStatus.REJECTED,
                false,
                LocalDateTime.now().minusDays(1)
        ));
        Verification latest = verificationRepository.save(verification(
                member,
                "latest-image",
                VerificationStatus.PENDING,
                true,
                LocalDateTime.now()
        ));
        verificationRepository.save(verification(
                otherMember,
                "other-image",
                VerificationStatus.PENDING,
                true,
                LocalDateTime.now().plusDays(1)
        ));

        // when
        List<Verification> found = verificationRepository.findAllByMemberIdOrderBySubmittedAtDesc(member.getId());

        // then
        assertThat(found).extracting(Verification::getId)
                .containsExactly(latest.getId(), old.getId());
    }

    private Member member(String email) {
        return Member.builder()
                .email(email)
                .password("password")
                .name("Test User")
                .role(Role.USER)
                .provider("LOCAL")
                .build();
    }

    private Verification verification(
            Member member,
            String imageUrl,
            VerificationStatus status,
            boolean current
    ) {
        return Verification.builder()
                .member(member)
                .imageUrl(imageUrl)
                .status(status)
                .submittedAt(LocalDateTime.now())
                .isCurrent(current)
                .build();
    }

    private Verification verification(
            Member member,
            String imageUrl,
            VerificationStatus status,
            boolean current,
            LocalDateTime submittedAt
    ) {
        return Verification.builder()
                .member(member)
                .imageUrl(imageUrl)
                .status(status)
                .submittedAt(submittedAt)
                .isCurrent(current)
                .build();
    }
}
