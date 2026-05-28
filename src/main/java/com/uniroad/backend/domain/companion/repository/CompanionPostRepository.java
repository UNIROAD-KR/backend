package com.uniroad.backend.domain.companion.repository;

import com.uniroad.backend.domain.companion.entity.CompanionPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanionPostRepository extends JpaRepository<CompanionPost, Long> {
    List<CompanionPost> findAllByOrderByCreatedAtDesc();
    List<CompanionPost> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
