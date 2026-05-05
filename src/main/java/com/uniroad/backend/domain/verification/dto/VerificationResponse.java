package com.uniroad.backend.domain.verification.dto;

import com.uniroad.backend.domain.verification.entity.Verification;
import com.uniroad.backend.domain.verification.entity.VerificationStatus;

import java.time.LocalDateTime;

public record VerificationResponse(
        Long id,
        String imageUrl,
        VerificationStatus status,
        String rejectReason,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt
) {
    public static VerificationResponse from(Verification verification) {
        return new VerificationResponse(
                verification.getId(),
                verification.getImageUrl(),
                verification.getStatus(),
                verification.getRejectReason(),
                verification.getSubmittedAt(),
                verification.getReviewedAt()
        );
    }
}
