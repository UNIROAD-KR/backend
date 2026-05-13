package com.uniroad.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 일반 로그인 요청 DTO
 */
@Schema(description = "로그인 요청 데이터")
public record LoginRequest(

        @Schema(description = "아이디", example = "user123")
        @NotBlank(message = "아이디는 필수입니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {}
