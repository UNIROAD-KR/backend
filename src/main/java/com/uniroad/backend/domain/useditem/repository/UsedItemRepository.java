package com.uniroad.backend.domain.useditem.repository;

import com.uniroad.backend.domain.useditem.entity.UsedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsedItemRepository extends JpaRepository<UsedItem, Long> {

    /**
     * 조회자의 지역과 일치하는 게시글을 우선적으로 보여주고, 그 다음으로 최신순으로 정렬
     */
    @Query("SELECT u FROM UsedItem u " +
           "ORDER BY CASE WHEN u.region = :userRegion THEN 0 ELSE 1 END ASC, " +
           "u.createdAt DESC")
    List<UsedItem> findAllSortedByRegion(@Param("userRegion") String userRegion);

    /**
     * 기본 최신순 정렬
     */
    List<UsedItem> findAllByOrderByCreatedAtDesc();
}
