package com.uniroad.backend.domain.useditem.dto;

import com.uniroad.backend.domain.useditem.entity.TradeItemCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCategoryImageRequestDto {

    @NotNull
    private TradeItemCategory category;

    @NotBlank
    private String imageUrl;
}