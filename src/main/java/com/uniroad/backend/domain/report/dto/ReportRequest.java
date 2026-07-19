package com.uniroad.backend.domain.report.dto;

import com.uniroad.backend.domain.report.entity.ReportReason;
import com.uniroad.backend.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "신고 요청")
public record ReportRequest(
        @Schema(description = "신고 대상 타입", example = "FREE_POST")
        @NotNull(message = "신고 대상 타입은 필수입니다.")
        ReportTargetType targetType,

        @Schema(description = "신고 대상 ID", example = "10")
        @NotNull(message = "신고 대상 ID는 필수입니다.")
        Long targetId,

        @Schema(description = "신고 사유", example = "SPAM")
        @NotNull(message = "신고 사유는 필수입니다.")
        ReportReason reason,

        @Schema(description = "상세 사유", example = "광고성 게시물입니다.")
        String detail
) {
}
