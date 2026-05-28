package com.uniroad.backend.domain.community.freepost.dto;

public record FreePostLikeResponse(
        boolean liked,
        long likeCount
) {
}
