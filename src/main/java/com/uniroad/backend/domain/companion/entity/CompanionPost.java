package com.uniroad.backend.domain.companion.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "companion_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanionPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String chatLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentStatus status;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentParticipants;

    @Column
    private String genderRatio;

    public void update(String title, String content, LocalDate startDate, LocalDate endDate,
                       String country, String region, String chatLink,
                       RecruitmentStatus status, Integer capacity, Integer currentParticipants, String genderRatio) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.country = country;
        this.region = region;
        this.chatLink = chatLink;
        this.status = status;
        this.capacity = capacity;
        this.currentParticipants = currentParticipants;
        this.genderRatio = genderRatio;
    }
}
