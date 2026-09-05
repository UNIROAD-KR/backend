package com.uniroad.backend.domain.blog.repository;

import com.uniroad.backend.domain.blog.entity.BlogPost;
import com.uniroad.backend.domain.blog.entity.BlogPostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Optional<BlogPost> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /** 관리자 목록 — 초안까지 최신순으로 전부 */
    List<BlogPost> findAllByOrderByIdDesc();

    /**
     * 공개 목록 — id 역순 커서 페이지네이션.
     * 공개 시각이 아니라 id를 커서로 쓰는 이유는 자유게시판과 방식을 맞추기 위해서다.
     */
    @Query("""
            SELECT b
            FROM BlogPost b
            WHERE b.status = :status
              AND (:cursorId IS NULL OR b.id < :cursorId)
            ORDER BY b.id DESC
            """)
    List<BlogPost> findPublishedByCursor(
            @Param("status") BlogPostStatus status,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
