package com.uniroad.backend.domain.useditem.repository;

import com.uniroad.backend.domain.useditem.entity.UsedItemPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsedItemRepository extends JpaRepository<UsedItemPost, Long> {

    @Query("""
        SELECT u
        FROM UsedItemPost u
        ORDER BY
            CASE WHEN u.region = :userRegion THEN 0 ELSE 1 END,
            u.createdAt DESC
    """)
    List<UsedItemPost> findAllSortedByRegion(@Param("userRegion") String userRegion);

    List<UsedItemPost> findAllByOrderByCreatedAtDesc();
}
