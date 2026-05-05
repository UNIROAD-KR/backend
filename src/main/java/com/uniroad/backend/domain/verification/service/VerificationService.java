package com.uniroad.backend.domain.verification.service;

import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.verification.dto.AdminVerificationResponse;
import com.uniroad.backend.domain.verification.dto.VerificationResponse;
import com.uniroad.backend.domain.verification.entity.Verification;
import com.uniroad.backend.domain.verification.entity.VerificationStatus;
import com.uniroad.backend.domain.verification.repository.VerificationRepository;
import com.uniroad.backend.domain.member.entity.Member;
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

    @Transactional
    public void approveVerification(Long verificationId) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 인증 요청입니다."));
        
        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new IllegalStateException("대기 중인 요청만 승인할 수 있습니다.");
        }

        verification.approve();
    }

    @Transactional
    public void rejectVerification(Long verificationId, String reason) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 인증 요청입니다."));

        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new IllegalStateException("대기 중인 요청만 거절할 수 있습니다.");
        }

        verification.reject(reason);
    }
}
