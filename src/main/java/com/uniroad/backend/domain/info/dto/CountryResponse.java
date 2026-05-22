package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.Country;

public record CountryResponse(
        Long id,
        String code,
        String name
) {
    public static CountryResponse from(Country country) {
        return new CountryResponse(
                country.getId(),
                country.getCode(),
                country.getName()
        );
    }
}
