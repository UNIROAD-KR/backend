package com.uniroad.backend.domain.member.dto;

import com.uniroad.backend.domain.member.entity.Role;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(
        @NotNull Role role
) {
}
