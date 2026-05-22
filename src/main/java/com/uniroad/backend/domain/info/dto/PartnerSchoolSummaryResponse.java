package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.PartnerUniversity;

import java.math.BigDecimal;
import java.util.List;

public record PartnerSchoolSummaryResponse(
        Long id,
        String name,
        String country,
        String city,
        BigDecimal rating,
        List<String> tags,
        String thumbnailImageUrl
) {
    public static PartnerSchoolSummaryResponse from(PartnerUniversity university) {
        return new PartnerSchoolSummaryResponse(
                university.getId(),
                university.getName(),
                university.getCountry().getName(),
                university.getCity(),
                university.getAvgRating(),
                university.getTags() == null ? List.of() : university.getTags(),
                university.getThumbnailUrl()
        );
    }
}
