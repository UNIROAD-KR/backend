package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.CountryResponse;
import com.uniroad.backend.domain.info.dto.PopularCountryResponse;
import com.uniroad.backend.domain.info.repository.CountryRepository;
import com.uniroad.backend.domain.info.repository.PartnerUniversityRepository;
import com.uniroad.backend.domain.info.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryService {

    private final CountryRepository countryRepository;
    private final PartnerUniversityRepository partnerUniversityRepository;
    private final ReviewRepository reviewRepository;

    public List<CountryResponse> getCountries() {
        return countryRepository.findAll().stream()
                .map(CountryResponse::from)
                .toList();
    }

    public List<PopularCountryResponse> getPopularCountries() {
        return countryRepository.findPopularCountries().stream()
                .map(country -> PopularCountryResponse.from(
                        country,
                        partnerUniversityRepository.countByCountryId(country.getId()),
                        reviewRepository.countByCountryId(country.getId())
                ))
                .toList();
    }
}
