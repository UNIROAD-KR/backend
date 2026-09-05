package com.uniroad.backend.domain.member.repository;

import com.uniroad.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 공지 알림처럼 전 회원에게 뿌릴 때 쓴다.
     * findAll()은 Member 엔티티를 통째로 올려 회원이 늘면 그대로 메모리를 먹는다.
     */
    @org.springframework.data.jpa.repository.Query("SELECT m.id FROM Member m")
    java.util.List<Long> findAllMemberIds();

    Optional<Member> findByEmail(String email);
    Optional<Member> findByUsername(String username);
    List<Member> findAllByOrderByCreatedAtDesc();
}
