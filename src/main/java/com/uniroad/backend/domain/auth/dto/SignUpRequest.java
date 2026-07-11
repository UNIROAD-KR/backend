package com.uniroad.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 일반 회원가입 요청 DTO
 */
@Schema(description = "회원가입 요청 데이터")
public record SignUpRequest(

        @Schema(description = "아이디", example = "user123")
        @NotBlank(message = "아이디는 필수입니다.")
        String username,

        @Schema(description = "이메일 주소 (선택)", example = "user@greenpath.seoul.kr")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "비밀번호 (8~20자 영문과 숫자 조합)", example = "Password123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
        @jakarta.validation.constraints.Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$",
            message = "비밀번호는 8~20자의 영문과 숫자 조합이어야 합니다."
        )
        String password,

        @Schema(description = "사용자 이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 1, max = 20, message = "이름은 1~20자여야 합니다.")
        String name
) {}
