package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.PartnerUniversity;

import java.math.BigDecimal;

public record PartnerUniversitySummaryResponse(
        Long id,
        String name,
        String country,
        String city,
        String thumbnailUrl,
        BigDecimal avgRating,
        Integer reviewCount
) {
    public static PartnerUniversitySummaryResponse from(PartnerUniversity university) {
        return new PartnerUniversitySummaryResponse(
                university.getId(),
                university.getName(),
                university.getCountry().getName(),
                university.getCity(),
                university.getThumbnailUrl(),
                university.getAvgRating(),
                university.getReviewCount()
        );
    }
}
