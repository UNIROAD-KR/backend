package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.CountryResponse;
import com.uniroad.backend.domain.info.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryService {

    private final CountryRepository countryRepository;

    public List<CountryResponse> getCountries() {
        return countryRepository.findAll().stream()
                .map(CountryResponse::from)
                .toList();
    }
}
