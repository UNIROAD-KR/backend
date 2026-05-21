package com.uniroad.backend.domain.member.controller;

import com.uniroad.backend.domain.member.dto.MemberResponseDto;
import com.uniroad.backend.domain.member.service.MemberService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 관련 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<MemberResponseDto> getMyInfo() {
        MemberResponseDto response = memberService.getMyInfo();
        return ApiResponse.success(response);
    }
}
