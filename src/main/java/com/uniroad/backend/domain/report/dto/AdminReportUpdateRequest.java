package com.uniroad.backend.domain.report.dto;

import com.uniroad.backend.domain.report.entity.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 신고 상태 변경 요청")
public record AdminReportUpdateRequest(
        @Schema(description = "신고 상태", example = "IN_PROGRESS")
        @NotNull(message = "신고 상태는 필수입니다.")
        ReportStatus status,

        @Schema(description = "관리자 메모", example = "검토 완료")
        String adminMemo
) {
}
