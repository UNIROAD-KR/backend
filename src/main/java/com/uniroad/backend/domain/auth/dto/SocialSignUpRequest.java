package com.uniroad.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "소셜 계정 전용 회원가입(아이디/비밀번호 설정) 요청 데이터")
public record SocialSignUpRequest(

        @Schema(description = "아이디", example = "user123")
        @NotBlank(message = "아이디는 필수입니다.")
        String username,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "비밀번호 (8~20자 영문과 숫자 조합)", example = "Password123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
        @jakarta.validation.constraints.Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$",
            message = "비밀번호는 8~20자의 영문과 숫자 조합이어야 합니다."
        )
        String password,

        @Schema(description = "이메일 주소 (선택 - 비워둘 시 기존 소셜 이메일 유지 또는 미설정)", example = "user@greenpath.seoul.kr")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {}
