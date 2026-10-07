package com.uniroad.backend.domain.useditem.dto;

import com.uniroad.backend.domain.useditem.entity.UsedItemStatus;
import com.uniroad.backend.global.common.SortOrder;

public record UsedItemSearchRequest(
        String title,
        String country,
        String region,
        String content,
        UsedItemStatus status,
        Long minPrice,
        Long maxPrice,
        SortOrder sort
) {
}
