package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.ScholarshipResponse;
import com.uniroad.backend.domain.info.repository.ScholarshipRepository;
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

    public Page<ScholarshipResponse> getScholarships(String countryCode, String keyword, Pageable pageable) {
        return scholarshipRepository.search(normalize(countryCode), normalize(keyword), pageable)
                .map(ScholarshipResponse::from);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
