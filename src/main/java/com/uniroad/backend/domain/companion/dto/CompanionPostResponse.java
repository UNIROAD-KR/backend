package com.uniroad.backend.domain.companion.dto;

import com.uniroad.backend.domain.companion.entity.CompanionPost;
import com.uniroad.backend.domain.companion.entity.RecruitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "동행 구하기 게시글 응답")
public record CompanionPostResponse(
    Long id,
    String memberName,
    String title,
    String content,
    LocalDate startDate,
    LocalDate endDate,
    String country,
    String region,
    String chatLink,
    RecruitmentStatus status,
    String statusDescription,
    Integer capacity,
    Integer currentParticipants,
    String genderRatio,
    LocalDateTime createdAt
) {
    public static CompanionPostResponse from(CompanionPost post) {
        return new CompanionPostResponse(
            post.getId(),
            post.getMember().getName(),
            post.getTitle(),
            post.getContent(),
            post.getStartDate(),
            post.getEndDate(),
            post.getCountry(),
            post.getRegion(),
            post.getChatLink(),
            post.getStatus(),
            post.getStatus().getDescription(),
            post.getCapacity(),
            post.getCurrentParticipants(),
            post.getGenderRatio(),
            post.getCreatedAt()
        );
    }
}
