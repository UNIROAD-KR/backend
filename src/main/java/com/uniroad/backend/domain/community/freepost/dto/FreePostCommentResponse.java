package com.uniroad.backend.domain.community.freepost.dto;

import com.uniroad.backend.domain.community.freepost.entity.FreePostComment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "자유게시판 댓글. 일반 댓글은 replies에 답글을 오래된 순으로 담는다.")
public record FreePostCommentResponse(
        Long id,
        @Schema(description = "원댓글 ID. 일반 댓글이면 null")
        Long parentId,
        String authorName,
        @Schema(description = "댓글 내용. 삭제된 댓글이면 \"삭제된 댓글입니다\"")
        String content,
        LocalDateTime createdAt,
        boolean mine,
        @Schema(description = "삭제된 댓글 여부. 답글이 남아 있어 자리만 남은 경우다")
        boolean deleted,
        @Schema(description = "답글 목록. 답글 자신은 항상 빈 배열이다")
        List<FreePostCommentResponse> replies
) {
    public static final String DELETED_CONTENT = "삭제된 댓글입니다";

    public static FreePostCommentResponse from(FreePostComment comment, Long memberId) {
        return from(comment, memberId, List.of());
    }

    public static FreePostCommentResponse from(FreePostComment comment, Long memberId, List<FreePostCommentResponse> replies) {
        boolean deleted = comment.isDeleted();
        return new FreePostCommentResponse(
                comment.getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                "익명",
                deleted ? DELETED_CONTENT : comment.getContent(),
                comment.getCreatedAt(),
                !deleted && comment.getMember().getId().equals(memberId),
                deleted,
                replies
        );
    }
}
