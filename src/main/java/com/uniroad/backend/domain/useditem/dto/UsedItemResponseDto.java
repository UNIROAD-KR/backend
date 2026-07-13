package com.uniroad.backend.domain.useditem.dto;

import com.uniroad.backend.domain.useditem.entity.UsedItemPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedItemResponseDto {

    private Long id;

    private String title;

    private String content;

    private Long price;

    private String region;

    private String semester;

    private String country;

    private Long memberId;

    private String authorName;

    private String authorNickname;

    private String authorDispatchedCountry;

    private String authorDispatchedRegion;

    private String authorDispatchedUniversity;

    private Integer authorDispatchYear;

    private String authorDispatchSemester;

    private LocalDate authorDispatchStartDate;

    private String thumbnailImageUrl;

    private List<TradeItemResponseDto> items;

    private List<TradeCategoryImageResponseDto> categoryImages;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static UsedItemResponseDto from(UsedItemPost usedItemPost) {

        return UsedItemResponseDto.builder()
                .id(usedItemPost.getId())
                .title(usedItemPost.getTitle())
                .content(usedItemPost.getContent())
                .price(usedItemPost.getPrice())
                .region(usedItemPost.getRegion())
                .semester(usedItemPost.getSemester())
                .country(usedItemPost.getCountry())
                .memberId(usedItemPost.getAuthor().getId())
                .authorName(usedItemPost.getAuthor().getName())
                .authorNickname(usedItemPost.getAuthor().getNickname())
                .authorDispatchedCountry(usedItemPost.getAuthor().getDispatchedCountry())
                .authorDispatchedRegion(usedItemPost.getAuthor().getDispatchedRegion())
                .authorDispatchedUniversity(usedItemPost.getAuthor().getDispatchedUniversity())
                .authorDispatchYear(usedItemPost.getAuthor().getDispatchYear())
                .authorDispatchSemester(usedItemPost.getAuthor().getDispatchSemester())
                .authorDispatchStartDate(usedItemPost.getAuthor().getDispatchStartDate())
                .thumbnailImageUrl(usedItemPost.getThumbnailImageUrl())

                .items(
                        usedItemPost.getItems().stream()
                                .map(item -> TradeItemResponseDto.builder()
                                        .category(item.getCategory())
                                        .name(item.getName())
                                        .quantity(item.getQuantity())
                                        .build())
                                .toList()
                )

                .categoryImages(
                        usedItemPost.getImages().stream()
                                .map(image -> TradeCategoryImageResponseDto.builder()
                                        .category(image.getCategory())
                                        .imageUrl(image.getImageUrl())
                                        .build())
                                .toList()
                )

                .createdAt(usedItemPost.getCreatedAt())
                .updatedAt(usedItemPost.getUpdatedAt())
                .build();
    }
}
