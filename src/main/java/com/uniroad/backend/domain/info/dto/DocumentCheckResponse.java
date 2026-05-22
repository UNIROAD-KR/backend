package com.uniroad.backend.domain.info.dto;

public record DocumentCheckResponse(
        Long id,
        String text,
        boolean checkedByMe
) {
}
