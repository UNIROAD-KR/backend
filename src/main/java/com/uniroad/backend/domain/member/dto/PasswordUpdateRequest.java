package com.uniroad.backend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordUpdateRequest(
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$",
                message = "새 비밀번호는 8~20자이며 영문과 숫자를 각각 1개 이상 포함해야 합니다."
        )
        String newPassword
) {
}
