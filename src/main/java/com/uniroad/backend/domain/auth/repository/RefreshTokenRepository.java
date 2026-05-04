package com.uniroad.backend.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;

import com.uniroad.backend.domain.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByMemberId(Long memberId);

    boolean existsByToken(String token);

    void deleteByMemberId(Long memberId);

    void deleteByToken(String token);
}
