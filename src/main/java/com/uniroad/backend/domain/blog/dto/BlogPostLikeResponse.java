package com.uniroad.backend.domain.blog.dto;

public record BlogPostLikeResponse(
        Long postId,
        boolean liked,
        long likeCount
) {
}
