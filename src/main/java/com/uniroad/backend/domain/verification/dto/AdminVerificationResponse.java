package com.uniroad.backend.domain.verification.dto;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.verification.entity.Verification;

public record AdminVerificationResponse(
        Long memberId,
        String memberName,
        String memberEmail,
        VerificationResponse verification
) {
    public static AdminVerificationResponse of(Member member, Verification verification) {
        return new AdminVerificationResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                VerificationResponse.from(verification)
        );
    }
}
