package com.uniroad.backend.domain.community.freepost.repository;

import com.uniroad.backend.domain.community.freepost.entity.FreePostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreePostLikeRepository extends JpaRepository<FreePostLike, Long> {
    boolean existsByFreePostIdAndMemberId(Long freePostId, Long memberId);
    long countByFreePostId(Long freePostId);
    Optional<FreePostLike> findByFreePostIdAndMemberId(Long freePostId, Long memberId);
    void deleteAllByFreePostId(Long freePostId);
    void deleteByMemberId(Long memberId);
}
