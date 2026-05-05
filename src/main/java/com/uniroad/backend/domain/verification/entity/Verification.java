package com.uniroad.backend.domain.verification.entity;

import com.uniroad.backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    private String rejectReason;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @Column(nullable = false)
    private boolean isCurrent;


    public void approve() {
        this.status = VerificationStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.status = VerificationStatus.REJECTED;
        this.rejectReason = reason;
        this.reviewedAt = LocalDateTime.now();
    }

    public void markNotCurrent() {
        this.isCurrent = false;
    }

    public static Verification create(Member member, String imageUrl) {
        return Verification.builder()
                .member(member)
                .imageUrl(imageUrl)
                .status(VerificationStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .isCurrent(true)
                .build();
    }
}
