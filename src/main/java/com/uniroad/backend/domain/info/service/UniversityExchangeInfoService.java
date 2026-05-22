package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.UniversityExchangeInfoResponse;
import com.uniroad.backend.domain.info.entity.UniversityExchangeInfo;
import com.uniroad.backend.domain.info.repository.UniversityExchangeInfoRepository;
import com.uniroad.backend.domain.info.repository.UniversityRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityExchangeInfoService {

    private final UniversityRepository universityRepository;
    private final UniversityExchangeInfoRepository universityExchangeInfoRepository;

    public UniversityExchangeInfoResponse getExchangeInfo(Long universityId) {
        if (!universityRepository.existsById(universityId)) {
            throw new CustomException(ErrorCode.UNIVERSITY_NOT_FOUND);
        }

        UniversityExchangeInfo exchangeInfo = universityExchangeInfoRepository.findByUniversityId(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_INFO_NOT_FOUND));

        return UniversityExchangeInfoResponse.from(exchangeInfo);
    }
}
