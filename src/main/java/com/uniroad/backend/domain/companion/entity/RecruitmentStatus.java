package com.uniroad.backend.domain.companion.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitmentStatus {
    RECRUITING("모집 중"),
    COMPLETED("모집 완료");

    private final String description;
}
