package com.uniroad.backend.domain.ticket.repository;

import com.uniroad.backend.domain.ticket.entity.TicketTransferPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketTransferRepository extends JpaRepository<TicketTransferPost, Long> {

    @Query("""
            SELECT t
            FROM TicketTransferPost t
            WHERE (:cursorId IS NULL OR t.id < :cursorId)
            ORDER BY t.id DESC
            """)
    List<TicketTransferPost> findByCursor(
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM TicketTransferPost t
            WHERE t.author.id = :memberId
              AND (:cursorId IS NULL OR t.id < :cursorId)
            ORDER BY t.id DESC
            """)
    List<TicketTransferPost> findByAuthorIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    void deleteByAuthorId(Long memberId);
}
