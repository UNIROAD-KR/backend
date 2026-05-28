package com.uniroad.backend.domain.community.freepost.repository;

import com.uniroad.backend.domain.community.freepost.entity.FreePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreePostRepository extends JpaRepository<FreePost, Long> {

    List<FreePost> findAllByOrderByCreatedAtDesc();
}
