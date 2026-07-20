package com.uniroad.backend.domain.useditem.dto;

import com.uniroad.backend.domain.useditem.entity.UsedItemStatus;

public record UsedItemSearchRequest(
        String title,
        String country,
        String region,
        String content,
        UsedItemStatus status
) {
}
