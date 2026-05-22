package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.ExchangeBlogLink;
import com.uniroad.backend.domain.info.entity.ExchangeSchedule;
import com.uniroad.backend.domain.info.entity.ExchangeTip;
import com.uniroad.backend.domain.info.entity.PartnerUniversity;
import com.uniroad.backend.domain.info.entity.UniversityExchangeInfo;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public record MyUniversityExchangeInfoResponse(
        String universityName,
        String officeName,
        String phone,
        String email,
        List<String> eligibility,
        List<ScheduleResponse> schedules,
        List<DocumentCheckResponse> requiredDocuments,
        List<PartnerSchool> partnerSchools,
        List<TipResponse> tips,
        List<BlogLinkResponse> blogLinks
) {
    public static MyUniversityExchangeInfoResponse from(
            UniversityExchangeInfo exchangeInfo,
            List<PartnerUniversity> partnerUniversities,
            Set<Long> checkedDocumentIds
    ) {
        return new MyUniversityExchangeInfoResponse(
                exchangeInfo.getUniversity().getName(),
                exchangeInfo.getOfficeName(),
                exchangeInfo.getPhone(),
                exchangeInfo.getEmail(),
                splitLines(exchangeInfo.getEligibility()),
                mapSchedules(exchangeInfo.getSchedules()),
                mapDocuments(exchangeInfo.getRequiredDocuments(), checkedDocumentIds),
                partnerUniversities.stream().map(PartnerSchool::from).toList(),
                mapTips(exchangeInfo.getTips()),
                mapBlogLinks(exchangeInfo.getBlogLinks())
        );
    }

    private static List<String> splitLines(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return value.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private static List<ScheduleResponse> mapSchedules(List<ExchangeSchedule> schedules) {
        if (schedules == null) {
            return List.of();
        }
        return schedules.stream().map(schedule -> new ScheduleResponse(
                schedule.getTitle(),
                formatPeriod(schedule)
        )).toList();
    }

    private static String formatPeriod(ExchangeSchedule schedule) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        if (schedule.getStartDate() == null && schedule.getEndDate() == null) {
            return null;
        }
        if (schedule.getStartDate() == null) {
            return schedule.getEndDate().format(formatter);
        }
        if (schedule.getEndDate() == null) {
            return schedule.getStartDate().format(formatter);
        }
        return schedule.getStartDate().format(formatter) + " ~ " + schedule.getEndDate().format(formatter);
    }

    private static List<DocumentCheckResponse> mapDocuments(List<String> documents, Set<Long> checkedDocumentIds) {
        if (documents == null) {
            return List.of();
        }
        return IntStream.range(0, documents.size())
                .mapToObj(index -> {
                    long id = index + 1L;
                    return new DocumentCheckResponse(id, documents.get(index), checkedDocumentIds.contains(id));
                })
                .toList();
    }

    private static List<TipResponse> mapTips(List<ExchangeTip> tips) {
        if (tips == null) {
            return List.of();
        }
        return tips.stream()
                .map(tip -> new TipResponse(tip.getTitle(), tip.getContent()))
                .toList();
    }

    private static List<BlogLinkResponse> mapBlogLinks(List<ExchangeBlogLink> blogLinks) {
        if (blogLinks == null) {
            return List.of();
        }
        return blogLinks.stream()
                .map(link -> new BlogLinkResponse(link.getTitle(), link.getUrl()))
                .toList();
    }

    public record ScheduleResponse(String title, String period) {
    }

    public record PartnerSchool(Long id, String name, String country, String city, BigDecimal rating) {
        public static PartnerSchool from(PartnerUniversity university) {
            return new PartnerSchool(
                    university.getId(),
                    university.getName(),
                    university.getCountry().getName(),
                    university.getCity(),
                    university.getAvgRating()
            );
        }
    }

    public record TipResponse(String title, String content) {
    }

    public record BlogLinkResponse(String title, String url) {
    }
}
