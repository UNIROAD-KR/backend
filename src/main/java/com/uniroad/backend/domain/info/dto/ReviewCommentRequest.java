package com.uniroad.backend.domain.info.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewCommentRequest(
        @NotBlank String content
) {
}
