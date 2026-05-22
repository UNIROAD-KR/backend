package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.Country;

public record PopularCountryResponse(
        String name,
        String code,
        Long schoolCount,
        Long reviewCount
) {
    public static PopularCountryResponse from(Country country, long schoolCount, long reviewCount) {
        return new PopularCountryResponse(
                country.getName(),
                country.getCode(),
                schoolCount,
                reviewCount
        );
    }
}
