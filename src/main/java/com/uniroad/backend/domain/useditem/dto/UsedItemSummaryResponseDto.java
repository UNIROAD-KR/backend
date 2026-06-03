package com.uniroad.backend.domain.useditem.dto;

import com.uniroad.backend.domain.useditem.entity.UsedItemPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String thumbnailImageUrl;

    private String authorName;

    private LocalDateTime updatedAt;

    public static UsedItemSummaryResponseDto from(UsedItemPost post) {

        return UsedItemSummaryResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .price(post.getPrice())
                .region(post.getRegion())
                .semester(post.getSemester())
                .thumbnailImageUrl(post.getThumbnailImageUrl())
                .authorName(post.getAuthor().getName())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

}
