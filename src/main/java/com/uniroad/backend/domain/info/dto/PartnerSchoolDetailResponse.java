package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.PartnerUniversity;

import java.math.BigDecimal;
import java.util.List;

public record PartnerSchoolDetailResponse(
        Long id,
        String name,
        String country,
        String city,
        BigDecimal rating,
        List<String> tags,
        List<String> imageUrls,
        BasicInfo basicInfo,
        LivingInfo livingInfo,
        boolean bookmarkedByMe
) {
    public static PartnerSchoolDetailResponse from(PartnerUniversity university, boolean bookmarkedByMe) {
        return new PartnerSchoolDetailResponse(
                university.getId(),
                university.getName(),
                university.getCountry().getName(),
                university.getCity(),
                university.getAvgRating(),
                university.getTags() == null ? List.of() : university.getTags(),
                resolveImageUrls(university),
                new BasicInfo(
                        join(university.getClassLanguages()),
                        join(university.getSupportedMajors()),
                        university.getSemesterSystem(),
                        university.getWebsiteUrl(),
                        university.getContact() != null ? university.getContact() : university.getInternationalOfficeEmail()
                ),
                new LivingInfo(
                        university.getDormitoryAvailable() == null ? university.getDormitoryType() : dormText(university),
                        university.getTransportAvg() == null ? null : university.getTransportAvg() + "원",
                        university.getCostLevel(),
                        university.getCostDescription(),
                        university.getNearbyEnvironment()
                ),
                bookmarkedByMe
        );
    }

    private static List<String> resolveImageUrls(PartnerUniversity university) {
        if (university.getImageUrls() != null && !university.getImageUrls().isEmpty()) {
            return university.getImageUrls();
        }
        return university.getThumbnailUrl() == null ? List.of() : List.of(university.getThumbnailUrl());
    }

    private static String join(List<String> values) {
        return values == null ? null : String.join(", ", values);
    }

    private static String dormText(PartnerUniversity university) {
        return Boolean.TRUE.equals(university.getDormitoryAvailable()) ? "기숙사 제공" : "기숙사 미제공";
    }

    public record BasicInfo(
            String language,
            String departments,
            String semesterSystem,
            String website,
            String contact
    ) {
    }

    public record LivingInfo(
            String dorm,
            String transport,
            String costLevel,
            String costDescription,
            String environment
    ) {
    }
}
