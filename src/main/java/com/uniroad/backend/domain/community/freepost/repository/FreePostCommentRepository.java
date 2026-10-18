package com.uniroad.backend.domain.community.freepost.repository;

import com.uniroad.backend.domain.community.freepost.entity.FreePostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FreePostCommentRepository extends JpaRepository<FreePostComment, Long> {
    List<FreePostComment> findByFreePostIdOrderByIdAsc(Long freePostId);

    /** 댓글 수. 답글도 포함하고, "삭제된 댓글입니다"로 남은 자리는 세지 않는다. */
    long countByFreePostIdAndDeletedFalse(Long freePostId);

    boolean existsByParentId(Long parentId);

    // 답글이 원댓글을 외래키로 가리키므로, 지울 때는 항상 답글을 먼저 지운다.
    // 파생 delete는 엔티티를 하나씩 지우며 순서를 보장하지 않아 JPQL로 순서를 정한다.

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM FreePostComment c WHERE c.freePost.id = :postId AND c.parent IS NOT NULL")
    void deleteRepliesByFreePostId(@Param("postId") Long postId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM FreePostComment c WHERE c.freePost.id = :postId")
    void deleteRemainingByFreePostId(@Param("postId") Long postId);

    /** 글을 지울 때 그 글의 댓글과 답글을 모두 지운다. */
    default void deleteAllByFreePostId(Long postId) {
        deleteRepliesByFreePostId(postId);
        deleteRemainingByFreePostId(postId);
    }

    @Query("SELECT c.id FROM FreePostComment c WHERE c.member.id = :memberId")
    List<Long> findIdsByMemberId(@Param("memberId") Long memberId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM FreePostComment c WHERE c.parent.id IN :parentIds")
    void deleteByParentIdIn(@Param("parentIds") Collection<Long> parentIds);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM FreePostComment c WHERE c.member.id = :memberId")
    void deleteRemainingByMemberId(@Param("memberId") Long memberId);

    /**
     * 탈퇴하는 회원의 댓글을 지운다. 그 댓글에 다른 사람이 단 답글도 함께 지운다.
     * 원댓글 작성자 행이 남지 않으면 답글만 남길 수 없기 때문이다.
     * MySQL은 같은 테이블을 서브쿼리로 읽으며 지우는 DELETE를 막아서(1093) id를 먼저 읽어 온다.
     */
    default void deleteAllWrittenBy(Long memberId) {
        List<Long> ids = findIdsByMemberId(memberId);
        if (ids.isEmpty()) {
            return;
        }
        deleteByParentIdIn(ids);
        deleteRemainingByMemberId(memberId);
    }
}
