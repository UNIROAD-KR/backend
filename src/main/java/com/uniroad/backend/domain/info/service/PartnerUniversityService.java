package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.PartnerUniversityDetailResponse;
import com.uniroad.backend.domain.info.dto.PartnerUniversitySummaryResponse;
import com.uniroad.backend.domain.info.entity.PartnerUniversity;
import com.uniroad.backend.domain.info.repository.PartnerUniversityRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerUniversityService {

    private final PartnerUniversityRepository partnerUniversityRepository;

    public Page<PartnerUniversitySummaryResponse> getPartnerUniversities(
            String countryCode,
            String keyword,
            String major,
            String language,
            Boolean dormitoryAvailable,
            Pageable pageable
    ) {
        return partnerUniversityRepository.search(
                        normalize(countryCode),
                        normalize(keyword),
                        normalize(major),
                        normalize(language),
                        dormitoryAvailable,
                        pageable
                )
                .map(PartnerUniversitySummaryResponse::from);
    }

    public PartnerUniversityDetailResponse getPartnerUniversity(Long id) {
        PartnerUniversity university = partnerUniversityRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNER_UNIVERSITY_NOT_FOUND));

        return PartnerUniversityDetailResponse.from(university);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
