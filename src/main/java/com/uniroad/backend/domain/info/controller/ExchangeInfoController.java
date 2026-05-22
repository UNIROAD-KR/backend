package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.PopularCountryResponse;
import com.uniroad.backend.domain.info.service.CountryService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "ExchangeInfo", description = "교환학생 정보 탐색 API")
@RestController
@RequestMapping("/api/exchange-info")
@RequiredArgsConstructor
public class ExchangeInfoController {

    private final CountryService countryService;

    @Operation(summary = "많이 찾는 국가 조회")
    @GetMapping("/popular-countries")
    public ResponseEntity<ApiResponse<List<PopularCountryResponse>>> getPopularCountries() {
        List<PopularCountryResponse> response = countryService.getPopularCountries();
        return ResponseEntity.ok(ApiResponse.success("인기 국가 조회 성공", response));
    }
}
