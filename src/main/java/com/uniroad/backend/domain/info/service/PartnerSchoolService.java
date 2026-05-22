package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.PartnerSchoolBookmarkResponse;
import com.uniroad.backend.domain.info.dto.PartnerSchoolDetailResponse;
import com.uniroad.backend.domain.info.dto.PartnerSchoolSummaryResponse;
import com.uniroad.backend.domain.info.entity.FavoritePartnerUniversity;
import com.uniroad.backend.domain.info.entity.PartnerUniversity;
import com.uniroad.backend.domain.info.repository.FavoritePartnerUniversityRepository;
import com.uniroad.backend.domain.info.repository.PartnerUniversityRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerSchoolService {

    private final PartnerUniversityRepository partnerUniversityRepository;
    private final FavoritePartnerUniversityRepository favoritePartnerUniversityRepository;
    private final MemberRepository memberRepository;

    public Page<PartnerSchoolSummaryResponse> getPartnerSchools(String keyword, String country, Pageable pageable) {
        return partnerUniversityRepository.search(
                        normalize(country),
                        normalize(keyword),
                        null,
                        null,
                        null,
                        pageable
                )
                .map(PartnerSchoolSummaryResponse::from);
    }

    public PartnerSchoolDetailResponse getPartnerSchool(Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        PartnerUniversity university = getPartnerUniversity(id);
        boolean bookmarkedByMe = favoritePartnerUniversityRepository.existsByPartnerUniversityIdAndMemberId(id, memberId);
        return PartnerSchoolDetailResponse.from(university, bookmarkedByMe);
    }

    @Transactional
    public PartnerSchoolBookmarkResponse bookmark(Long id) {
        PartnerUniversity university = getPartnerUniversity(id);
        Member member = getCurrentMember();

        if (!favoritePartnerUniversityRepository.existsByPartnerUniversityIdAndMemberId(id, member.getId())) {
            favoritePartnerUniversityRepository.save(FavoritePartnerUniversity.builder()
                    .partnerUniversity(university)
                    .member(member)
                    .build());
        }

        return new PartnerSchoolBookmarkResponse(true);
    }

    @Transactional
    public PartnerSchoolBookmarkResponse unbookmark(Long id) {
        getPartnerUniversity(id);
        Long memberId = SecurityUtil.getCurrentMemberId();
        favoritePartnerUniversityRepository.findByPartnerUniversityIdAndMemberId(id, memberId)
                .ifPresent(favoritePartnerUniversityRepository::delete);
        return new PartnerSchoolBookmarkResponse(false);
    }

    private PartnerUniversity getPartnerUniversity(Long id) {
        return partnerUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNER_UNIVERSITY_NOT_FOUND));
    }

    private Member getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
