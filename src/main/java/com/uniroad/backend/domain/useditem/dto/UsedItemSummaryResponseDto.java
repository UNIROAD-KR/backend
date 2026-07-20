package com.uniroad.backend.domain.useditem.dto;

import com.uniroad.backend.domain.useditem.entity.UsedItemPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedItemSummaryResponseDto {

    private Long id;

    private String title;

    private Long price;

    private String region;

    private String semester;

    private String country;
    private long scrapCount;

    private String thumbnailImageUrl;

    private String authorName;

    private String authorNickname;

    private String authorDispatchedCountry;

    private String authorDispatchedRegion;

    private String authorDispatchedUniversity;

    private Integer authorDispatchYear;

    private String authorDispatchSemester;

    private LocalDate authorDispatchStartDate;

    private LocalDateTime updatedAt;

    public static UsedItemSummaryResponseDto from(UsedItemPost post, long scrapCount) {

        return UsedItemSummaryResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .price(post.getPrice())
                .region(post.getRegion())
                .semester(post.getSemester())
                .country(post.getCountry())
                .scrapCount(scrapCount)
                .thumbnailImageUrl(post.getThumbnailImageUrl())
                .authorName(post.getAuthor().getName())
                .authorNickname(post.getAuthor().getNickname())
                .authorDispatchedCountry(post.getAuthor().getDispatchedCountry())
                .authorDispatchedRegion(post.getAuthor().getDispatchedRegion())
                .authorDispatchedUniversity(post.getAuthor().getDispatchedUniversity())
                .authorDispatchYear(post.getAuthor().getDispatchYear())
                .authorDispatchSemester(post.getAuthor().getDispatchSemester())
                .authorDispatchStartDate(post.getAuthor().getDispatchStartDate())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

}
