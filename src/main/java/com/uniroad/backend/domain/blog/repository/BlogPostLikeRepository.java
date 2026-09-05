package com.uniroad.backend.domain.blog.repository;

import com.uniroad.backend.domain.blog.entity.BlogPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BlogPostLikeRepository extends JpaRepository<BlogPostLike, Long> {

    boolean existsByBlogPostIdAndMemberId(Long blogPostId, Long memberId);

    long countByBlogPostId(Long blogPostId);

    Optional<BlogPostLike> findByBlogPostIdAndMemberId(Long blogPostId, Long memberId);

    void deleteAllByBlogPostId(Long blogPostId);

    void deleteByMemberId(Long memberId);

    /**
     * 목록에서 글마다 좋아요 수를 따로 세면 N+1이 된다. 한 번에 모아 받는다.
     * 반환은 [blogPostId, count] 배열이다.
     */
    @Query("""
            SELECT l.blogPost.id, COUNT(l.id)
            FROM BlogPostLike l
            WHERE l.blogPost.id IN :postIds
            GROUP BY l.blogPost.id
            """)
    List<Object[]> countByBlogPostIds(@Param("postIds") Collection<Long> postIds);

    /** 목록에서 "내가 누른 글"을 한 번에 가려낸다 */
    @Query("""
            SELECT l.blogPost.id
            FROM BlogPostLike l
            WHERE l.member.id = :memberId
              AND l.blogPost.id IN :postIds
            """)
    List<Long> findLikedPostIds(@Param("memberId") Long memberId, @Param("postIds") Collection<Long> postIds);
}
