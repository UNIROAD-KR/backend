package com.uniroad.backend.domain.member.repository;

import com.uniroad.backend.domain.member.entity.MemberSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSocialAccountRepository extends JpaRepository<MemberSocialAccount, Long> {
    Optional<MemberSocialAccount> findByProviderIgnoreCaseAndProviderId(String provider, String providerId);
    boolean existsByProviderIgnoreCaseAndProviderId(String provider, String providerId);
}
