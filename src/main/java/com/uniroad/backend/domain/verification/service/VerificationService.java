package com.uniroad.backend.domain.verification.service;

import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.notification.service.NotificationService;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.verification.dto.AdminVerificationResponse;
import com.uniroad.backend.domain.verification.dto.VerificationResponse;
import com.uniroad.backend.domain.verification.entity.Verification;
import com.uniroad.backend.domain.verification.entity.VerificationStatus;
import com.uniroad.backend.domain.verification.repository.VerificationRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    /**
     * 인증 서류 이미지에 접근할 수 있는지 확인한다.
     *
     * 비공개 버킷의 조회용 URL은 key만 알면 누구에게나 발급될 수 있으므로,
     * 관리자이거나 그 서류를 제출한 본인일 때만 허용한다.
     */
    public void validateImageAccess(Long memberId, String imageUrl) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() == Role.ADMIN) {
            return;
        }

        if (!verificationRepository.existsByMemberIdAndImageUrl(memberId, imageUrl)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    @Transactional
    public VerificationResponse submitVerification(Long memberId, String imageUrl) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 기존의 current 인증이 있다면 만료시킴
        verificationRepository.findByMemberAndIsCurrentTrue(member)
                .ifPresent(Verification::markNotCurrent);

        Verification verification = Verification.create(member, imageUrl);
        Verification savedVerification = verificationRepository.save(verification);
        return VerificationResponse.from(savedVerification);
    }

    public List<AdminVerificationResponse> getPendingVerifications() {
        return verificationRepository.findAllByStatusAndIsCurrentTrue(VerificationStatus.PENDING)
                .stream()
                .map(v -> AdminVerificationResponse.of(v.getMember(), v))
                .collect(Collectors.toList());
    }

    public List<AdminVerificationResponse> getApprovedVerifications() {
        return verificationRepository.findAllByStatusAndIsCurrentTrue(VerificationStatus.APPROVED)
                .stream()
                .map(v -> AdminVerificationResponse.of(v.getMember(), v))
                .collect(Collectors.toList());
    }

    public List<AdminVerificationResponse> getRejectedVerifications() {
        return verificationRepository.findAllByStatusAndIsCurrentTrue(VerificationStatus.REJECTED)
                .stream()
                .map(v -> AdminVerificationResponse.of(v.getMember(), v))
                .collect(Collectors.toList());
    }

    public List<VerificationResponse> getMyVerifications(Long memberId) {
        return verificationRepository.findAllByMemberIdOrderBySubmittedAtDesc(memberId)
                .stream()
                .map(VerificationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveVerification(Long verificationId) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 인증 요청입니다."));

        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new IllegalStateException("대기 중인 요청만 승인할 수 있습니다.");
        }

        verification.approve();

        Member member = verification.getMember();
        if (member.getRole() == Role.USER) {
            member.updateRole(Role.VERIFIED);
        }

        // 승인은 관리자가 임의의 시점에 하므로, 알려주지 않으면 사용자는 앱을 열어
        // 직접 확인하기 전까지 인증 회원 기능이 열린 줄 모른다.
        notificationService.notifyVerificationApproved(member, verification.getId());
    }

    @Transactional
    public void rejectVerification(Long verificationId, String reason) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 인증 요청입니다."));

        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new IllegalStateException("대기 중인 요청만 거절할 수 있습니다.");
        }

        verification.reject(reason);

        // 반려는 사용자가 다시 신청해야 끝나는 일이라, 알리지 않으면 아무 일도 일어나지 않는다.
        notificationService.notifyVerificationRejected(
                verification.getMember(), verification.getId(), reason);
    }
}
