package com.uniroad.backend.domain.member.service;

import com.uniroad.backend.domain.info.entity.University;
import com.uniroad.backend.domain.info.repository.UniversityRepository;
import com.uniroad.backend.domain.member.dto.MemberProfileUpdateRequest;
import com.uniroad.backend.domain.member.dto.MemberResponseDto;
import com.uniroad.backend.domain.member.dto.PasswordUpdateRequest;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberResponseDto getMyInfo() {
        return MemberResponseDto.from(getCurrentMember());
    }

    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        Member member = getCurrentMember();

        if (member.getPassword() == null || member.getPassword().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        member.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public MemberResponseDto updateMyProfile(MemberProfileUpdateRequest request) {
        Member member = getCurrentMember();
        University domesticUniversity = findOrCreateUniversity(request.domesticUniversity());

        member.updateProfile(
                request.currentSituation(),
                normalizeOptional(request.nickname()),
                normalizeOptional(request.dispatchedUniversity()),
                normalizeOptional(request.dispatchedCountry()),
                domesticUniversity
        );

        return MemberResponseDto.from(member);
    }

    private Member getCurrentMember() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private University findOrCreateUniversity(String universityName) {
        String normalizedName = normalizeOptional(universityName);
        if (normalizedName == null) {
            return null;
        }

        return universityRepository.findByName(normalizedName)
                .orElseGet(() -> universityRepository.save(
                        University.builder()
                                .name(normalizedName)
                                .build()
                ));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return normalized;
    }
}
