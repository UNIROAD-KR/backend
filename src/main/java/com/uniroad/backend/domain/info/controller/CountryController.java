package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.CountryResponse;
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

@Tag(name = "Country", description = "국가 코드 API")
@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @Operation(summary = "국가 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getCountries() {
        List<CountryResponse> response = countryService.getCountries();
        return ResponseEntity.ok(ApiResponse.success("국가 목록 조회 성공", response));
    }
}
