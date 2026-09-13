package com.uniroad.backend.domain.blog.repository;

import com.uniroad.backend.domain.blog.entity.BlogPost;
import com.uniroad.backend.domain.blog.entity.BlogPostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * 조회수만 올린다.
     *
     * 엔티티 필드를 고쳐 더티체킹에 맡기면 @LastModifiedDate가 함께 움직여 updatedAt이 "마지막 조회 시각"이 된다.
     * 그 값은 글 상세의 dateModified와 sitemap의 lastmod로 나가므로, 방문할 때마다 검색엔진에
     * "글이 방금 수정됐다"고 알리는 꼴이 된다. 감사 필드를 건드리지 않으려면 UPDATE 한 문장으로 처리해야 한다.
     *
     * 영속성 컨텍스트를 비우지 않는다(clearAutomatically). 비우면 호출부가 들고 있던 글이 준영속이 되어
     * 지연 로딩인 author를 읽는 순간 터진다. 대신 화면에 보일 조회수는 부르는 쪽에서 +1 해 넘긴다.
     */
    @Modifying
    @Query("UPDATE BlogPost b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void increaseViewCount(@Param("id") Long id);
}
