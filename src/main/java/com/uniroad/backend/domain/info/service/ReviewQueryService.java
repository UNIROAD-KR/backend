package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.ReviewSummaryResponse;
import com.uniroad.backend.domain.info.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    public Page<ReviewSummaryResponse> getReviews(Long partnerUniversityId, String countryCode, Pageable pageable) {
        return reviewRepository.search(partnerUniversityId, normalize(countryCode), pageable)
                .map(ReviewSummaryResponse::from);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
