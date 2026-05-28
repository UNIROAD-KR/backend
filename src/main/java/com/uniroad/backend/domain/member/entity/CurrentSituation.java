package com.uniroad.backend.domain.member.entity;

public enum CurrentSituation {
    PREPARING_APPLICATION("교환학생 지원 준비중"),
    PREPARING_DEPARTURE("합격 후 출국 준비중"),
    DISPATCHED("파견중");

    private final String description;

    CurrentSituation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
