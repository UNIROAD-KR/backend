package com.uniroad.backend.domain.useditem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedItemRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "설명은 필수입니다.")
    private String content;

    @NotNull(message = "가격은 필수입니다.")
    private Long price;

    @NotBlank(message = "거래 희망 지역은 필수입니다.")
    private String region;

    @NotBlank(message = "거래 학기는 필수입니다.")
    private String semester;

    private List<String> imageUrls;
}
