package com.uniroad.backend.domain.useditem.dto;

import com.uniroad.backend.domain.useditem.entity.UsedItem;
import com.uniroad.backend.domain.useditem.entity.UsedItemImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private String authorName;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static UsedItemResponseDto from(UsedItem usedItem) {
        return UsedItemResponseDto.builder()
                .id(usedItem.getId())
                .title(usedItem.getTitle())
                .content(usedItem.getContent())
                .price(usedItem.getPrice())
                .region(usedItem.getRegion())
                .semester(usedItem.getSemester())
                .authorName(usedItem.getAuthor().getName())
                .imageUrls(usedItem.getImages().stream()
                        .map(UsedItemImage::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(usedItem.getCreatedAt())
                .build();
    }
}
