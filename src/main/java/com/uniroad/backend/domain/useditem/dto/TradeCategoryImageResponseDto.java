package com.uniroad.backend.domain.useditem.dto;

import com.uniroad.backend.domain.useditem.entity.TradeItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCategoryImageResponseDto {
    private TradeItemCategory category;

    private String imageUrl;
}
