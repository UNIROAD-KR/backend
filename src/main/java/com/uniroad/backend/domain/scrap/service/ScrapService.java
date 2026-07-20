package com.uniroad.backend.domain.scrap.service;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.scrap.entity.Scrap;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import com.uniroad.backend.domain.scrap.repository.ScrapRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;

    public long count(ScrapTargetType targetType, Long targetId) {
        return scrapRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    public boolean exists(ScrapTargetType targetType, Long targetId, Long memberId) {
        return scrapRepository.existsByTargetTypeAndTargetIdAndMemberId(targetType, targetId, memberId);
    }

    @Transactional
    public boolean toggle(ScrapTargetType targetType, Long targetId) {
        Member member = getCurrentMember();
        return scrapRepository.findByTargetTypeAndTargetIdAndMemberId(targetType, targetId, member.getId())
                .map(scrap -> {
                    scrapRepository.delete(scrap);
                    return false;
                })
                .orElseGet(() -> {
                    scrapRepository.save(Scrap.builder()
                            .targetType(targetType)
                            .targetId(targetId)
                            .member(member)
                            .build());
                    return true;
                });
    }

    private Member getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
