package com.uniroad.backend.domain.verification.service;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.verification.dto.AdminVerificationResponse;
import com.uniroad.backend.domain.verification.dto.VerificationResponse;
import com.uniroad.backend.domain.verification.entity.Verification;
import com.uniroad.backend.domain.verification.entity.VerificationStatus;
import com.uniroad.backend.domain.verification.repository.VerificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @InjectMocks
    private VerificationService verificationService;

    @Mock
    private VerificationRepository verificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("submitVerification creates pending current verification")
    void submitVerification_Success() {
        // given
        Member member = member(1L);
        Verification previousVerification = verification(10L, member, "old-image", VerificationStatus.PENDING, true);

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(verificationRepository.findByMemberAndIsCurrentTrue(member)).willReturn(Optional.of(previousVerification));
        given(verificationRepository.save(any(Verification.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        VerificationResponse response = verificationService.submitVerification(1L, "new-image");

        // then
        assertThat(previousVerification.isCurrent()).isFalse();
        assertThat(response.imageUrl()).isEqualTo("new-image");
        assertThat(response.status()).isEqualTo(VerificationStatus.PENDING);
        assertThat(response.submittedAt()).isNotNull();

        ArgumentCaptor<Verification> captor = ArgumentCaptor.forClass(Verification.class);
        verify(verificationRepository).save(captor.capture());
        Verification saved = captor.getValue();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getImageUrl()).isEqualTo("new-image");
        assertThat(saved.isCurrent()).isTrue();
    }

    @Test
    @DisplayName("submitVerification fails when member does not exist")
    void submitVerification_Fail_MemberNotFound() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> verificationService.submitVerification(1L, "image"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getPendingVerifications returns admin responses")
    void getPendingVerifications_Success() {
        // given
        Member member = member(1L);
        Verification verification = verification(20L, member, "image", VerificationStatus.PENDING, true);
        given(verificationRepository.findAllByStatusAndIsCurrentTrue(VerificationStatus.PENDING))
                .willReturn(List.of(verification));

        // when
        List<AdminVerificationResponse> responses = verificationService.getPendingVerifications();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).memberId()).isEqualTo(1L);
        assertThat(responses.get(0).memberEmail()).isEqualTo("user1@test.com");
        assertThat(responses.get(0).verification().id()).isEqualTo(20L);
    }

    @Test
    @DisplayName("getMyVerifications returns member verification history")
    void getMyVerifications_Success() {
        // given
        Member member = member(1L);
        Verification latest = verification(2L, member, "latest-image", VerificationStatus.PENDING, true);
        Verification old = verification(1L, member, "old-image", VerificationStatus.REJECTED, false);
        given(verificationRepository.findAllByMemberIdOrderBySubmittedAtDesc(1L))
                .willReturn(List.of(latest, old));

        // when
        List<VerificationResponse> responses = verificationService.getMyVerifications(1L);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(2L);
        assertThat(responses.get(0).imageUrl()).isEqualTo("latest-image");
        assertThat(responses.get(1).id()).isEqualTo(1L);
        assertThat(responses.get(1).status()).isEqualTo(VerificationStatus.REJECTED);
    }

    @Test
    @DisplayName("approveVerification approves pending verification")
    void approveVerification_Success() {
        // given
        Verification verification = verification(1L, member(1L), "image", VerificationStatus.PENDING, true);
        given(verificationRepository.findById(1L)).willReturn(Optional.of(verification));

        // when
        verificationService.approveVerification(1L);

        // then
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.APPROVED);
        assertThat(verification.getReviewedAt()).isNotNull();
        assertThat(verification.getMember().getRole()).isEqualTo(Role.VERIFIED);
    }

    @Test
    @DisplayName("approveVerification fails when verification is not pending")
    void approveVerification_Fail_NotPending() {
        // given
        Verification verification = verification(1L, member(1L), "image", VerificationStatus.APPROVED, true);
        given(verificationRepository.findById(1L)).willReturn(Optional.of(verification));

        // when & then
        assertThatThrownBy(() -> verificationService.approveVerification(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("rejectVerification rejects pending verification")
    void rejectVerification_Success() {
        // given
        Verification verification = verification(1L, member(1L), "image", VerificationStatus.PENDING, true);
        given(verificationRepository.findById(1L)).willReturn(Optional.of(verification));

        // when
        verificationService.rejectVerification(1L, "invalid image");

        // then
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(verification.getRejectReason()).isEqualTo("invalid image");
        assertThat(verification.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("rejectVerification fails when verification does not exist")
    void rejectVerification_Fail_NotFound() {
        // given
        given(verificationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> verificationService.rejectVerification(1L, "reason"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Member member(Long id) {
        return Member.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .name("User " + id)
                .role(Role.USER)
                .provider("LOCAL")
                .build();
    }

    private Verification verification(
            Long id,
            Member member,
            String imageUrl,
            VerificationStatus status,
            boolean current
    ) {
        return Verification.builder()
                .id(id)
                .member(member)
                .imageUrl(imageUrl)
                .status(status)
                .submittedAt(LocalDateTime.now())
                .isCurrent(current)
                .build();
    }
}
