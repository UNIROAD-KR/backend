package com.uniroad.backend.domain.accountbook.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountCategory {
    FOOD("식비"),
    TRANSPORT("교통비"),
    SHOPPING("쇼핑"),
    TRAVEL("여행"),
    ETC("기타지출"),
    CHARGE("충전"); // 충전용 카테고리 추가

    private final String description;
}
