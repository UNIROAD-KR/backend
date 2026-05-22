package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.DocumentCheckRequest;
import com.uniroad.backend.domain.info.dto.DocumentCheckResponse;
import com.uniroad.backend.domain.info.dto.MyUniversityExchangeInfoResponse;
import com.uniroad.backend.domain.info.entity.UniversityExchangeDocumentCheck;
import com.uniroad.backend.domain.info.entity.UniversityExchangeInfo;
import com.uniroad.backend.domain.info.repository.PartnerUniversityRepository;
import com.uniroad.backend.domain.info.repository.UniversityExchangeDocumentCheckRepository;
import com.uniroad.backend.domain.info.repository.UniversityExchangeInfoRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyUniversityExchangeInfoService {

    private final MemberRepository memberRepository;
    private final UniversityExchangeInfoRepository universityExchangeInfoRepository;
    private final UniversityExchangeDocumentCheckRepository documentCheckRepository;
    private final PartnerUniversityRepository partnerUniversityRepository;

    public MyUniversityExchangeInfoResponse getMyExchangeInfo() {
        Member member = getCurrentMember();
        if (member.getDomesticUniversity() == null) {
            throw new CustomException(ErrorCode.UNIVERSITY_NOT_FOUND);
        }

        UniversityExchangeInfo exchangeInfo = getExchangeInfo(member);
        Set<Long> checkedDocumentIds = documentCheckRepository
                .findByExchangeInfoIdAndMemberId(exchangeInfo.getId(), member.getId())
                .stream()
                .filter(UniversityExchangeDocumentCheck::isChecked)
                .map(UniversityExchangeDocumentCheck::getDocumentId)
                .collect(Collectors.toSet());

        return MyUniversityExchangeInfoResponse.from(
                exchangeInfo,
                partnerUniversityRepository.findAll(PageRequest.of(0, 20)).getContent(),
                checkedDocumentIds
        );
    }

    @Transactional
    public DocumentCheckResponse updateDocumentCheck(Long documentId, DocumentCheckRequest request) {
        Member member = getCurrentMember();
        UniversityExchangeInfo exchangeInfo = getExchangeInfo(member);
        String documentText = getDocumentText(exchangeInfo, documentId);

        UniversityExchangeDocumentCheck check = documentCheckRepository
                .findByExchangeInfoIdAndMemberIdAndDocumentId(exchangeInfo.getId(), member.getId(), documentId)
                .orElseGet(() -> documentCheckRepository.save(UniversityExchangeDocumentCheck.builder()
                        .exchangeInfo(exchangeInfo)
                        .member(member)
                        .documentId(documentId)
                        .checked(false)
                        .build()));

        check.updateChecked(request.checked());
        return new DocumentCheckResponse(documentId, documentText, check.isChecked());
    }

    private String getDocumentText(UniversityExchangeInfo exchangeInfo, Long documentId) {
        if (exchangeInfo.getRequiredDocuments() == null
                || documentId < 1
                || documentId > exchangeInfo.getRequiredDocuments().size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return exchangeInfo.getRequiredDocuments().get(documentId.intValue() - 1);
    }

    private UniversityExchangeInfo getExchangeInfo(Member member) {
        if (member.getDomesticUniversity() == null) {
            throw new CustomException(ErrorCode.UNIVERSITY_NOT_FOUND);
        }
        return universityExchangeInfoRepository.findByUniversityId(member.getDomesticUniversity().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_INFO_NOT_FOUND));
    }

    private Member getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
