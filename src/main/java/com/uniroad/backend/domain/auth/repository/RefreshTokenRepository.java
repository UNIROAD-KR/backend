package com.uniroad.backend.domain.auth.repository;

import com.uniroad.backend.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByMemberId(Long memberId);

    boolean existsByToken(String token);

    void deleteByMemberId(Long memberId);

    void deleteByToken(String token);
}
