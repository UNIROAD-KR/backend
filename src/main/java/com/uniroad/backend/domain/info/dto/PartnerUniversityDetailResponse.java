package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.PartnerUniversity;

import java.math.BigDecimal;
import java.util.List;

public record PartnerUniversityDetailResponse(
        Long id,
        String name,
        String country,
        String city,
        String description,
        String websiteUrl,
        String thumbnailUrl,
        List<String> classLanguages,
        List<String> supportedMajors,
        Boolean creditTransferPossible,
        String internationalOfficeEmail,
        String internationalOfficeSnsUrl,
        BigDecimal minGpa,
        String languageRequirement,
        List<String> requiredDocuments,
        Boolean dormitoryAvailable,
        String dormitoryType,
        Integer dormitoryPrice,
        String housingDescription,
        String nearbyEnvironment,
        Integer rentAvg,
        Integer mealAvg,
        Integer transportAvg,
        BigDecimal avgRating,
        Integer reviewCount
) {
    public static PartnerUniversityDetailResponse from(PartnerUniversity university) {
        return new PartnerUniversityDetailResponse(
                university.getId(),
                university.getName(),
                university.getCountry(),
                university.getCity(),
                university.getDescription(),
                university.getWebsiteUrl(),
                university.getThumbnailUrl(),
                university.getClassLanguages(),
                university.getSupportedMajors(),
                university.getCreditTransferPossible(),
                university.getInternationalOfficeEmail(),
                university.getInternationalOfficeSnsUrl(),
                university.getMinGpa(),
                university.getLanguageRequirement(),
                university.getRequiredDocuments(),
                university.getDormitoryAvailable(),
                university.getDormitoryType(),
                university.getDormitoryPrice(),
                university.getHousingDescription(),
                university.getNearbyEnvironment(),
                university.getRentAvg(),
                university.getMealAvg(),
                university.getTransportAvg(),
                university.getAvgRating(),
                university.getReviewCount()
        );
    }
}
