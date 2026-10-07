package com.uniroad.backend.domain.companion.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderCondition {
    ANY("성별 무관"),
    FEMALE_ONLY("여성만"),
    MALE_ONLY("남성만");

    private final String description;
}
