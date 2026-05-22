package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.ScholarshipResponse;
import com.uniroad.backend.domain.info.entity.Scholarship;
import com.uniroad.backend.domain.info.repository.ScholarshipRepository;
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
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;

    public Page<ScholarshipResponse> getScholarships(String country, String keyword, Pageable pageable) {
        return scholarshipRepository.search(normalize(country), normalize(keyword), pageable)
                .map(ScholarshipResponse::from);
    }

    public ScholarshipResponse getScholarship(Long id) {
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        return ScholarshipResponse.from(scholarship);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
