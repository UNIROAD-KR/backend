package com.uniroad.backend.domain.community.freepost.repository;

import com.uniroad.backend.domain.community.freepost.entity.FreePostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreePostCommentRepository extends JpaRepository<FreePostComment, Long> {

    List<FreePostComment> findByFreePostIdOrderByCreatedAtAsc(Long freePostId);

    long countByFreePostId(Long freePostId);

    void deleteAllByFreePostId(Long freePostId);
}
