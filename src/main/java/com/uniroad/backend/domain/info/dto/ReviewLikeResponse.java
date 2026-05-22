package com.uniroad.backend.domain.info.dto;

public record ReviewLikeResponse(
        boolean likedByMe,
        Long likeCount
) {
}
