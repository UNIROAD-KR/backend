package com.uniroad.backend.domain.admin.dto;

import lombok.Builder;

@Builder
public record AdminDashboardResponse(
        long totalMembers,
        long todaySignups,
        long totalPosts,
        long pendingVerifications,
        long reportCount,
        long resolvedReportCount,
        long pendingReportCount
) {
}
