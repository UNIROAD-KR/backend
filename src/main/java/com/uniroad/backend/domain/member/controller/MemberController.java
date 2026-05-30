package com.uniroad.backend.domain.member.controller;

import com.uniroad.backend.domain.member.dto.MemberProfileUpdateRequest;
import com.uniroad.backend.domain.member.dto.MemberResponseDto;
import com.uniroad.backend.domain.member.dto.PasswordUpdateRequest;
import com.uniroad.backend.domain.member.service.MemberService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 API")
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

    @Operation(summary = "내 비밀번호 수정", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        memberService.updatePassword(request);
        return ApiResponse.success("비밀번호 수정 성공", null);
    }

    @Operation(summary = "내 프로필 수정", description = "현재 상태, 닉네임, 파견 국가, 파견 대학교, 현재 대학교를 수정합니다.")
    @PatchMapping("/me/profile")
    public ApiResponse<MemberResponseDto> updateMyProfile(
            @Valid @RequestBody MemberProfileUpdateRequest request
    ) {
        MemberResponseDto response = memberService.updateMyProfile(request);
        return ApiResponse.success("내 프로필 수정 성공", response);
    }
}
