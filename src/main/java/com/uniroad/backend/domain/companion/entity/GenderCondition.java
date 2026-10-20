package com.uniroad.backend.domain.companion.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * API로는 한글 문구('무관'/'여성만'/'남성만')를 주고받고, DB에는 enum 이름(ANY 등)으로 저장한다.
 */
@Getter
@RequiredArgsConstructor
public enum GenderCondition {
    ANY("무관"),
    FEMALE_ONLY("여성만"),
    MALE_ONLY("남성만");

    @JsonValue
    private final String description;

    /**
     * 한글 문구를 enum으로 바꾼다. 예전 클라이언트를 위해 enum 이름(ANY 등)도 받는다.
     */
    @JsonCreator
    public static GenderCondition from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(condition -> condition.description.equals(trimmed) || condition.name().equalsIgnoreCase(trimmed))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "성별 조건은 '무관', '여성만', '남성만' 중 하나여야 합니다."));
    }
}
