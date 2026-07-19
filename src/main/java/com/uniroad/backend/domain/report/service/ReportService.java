package com.uniroad.backend.domain.report.service;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.report.dto.AdminReportUpdateRequest;
import com.uniroad.backend.domain.report.dto.ReportRequest;
import com.uniroad.backend.domain.report.dto.ReportResponse;
import com.uniroad.backend.domain.report.entity.Report;
import com.uniroad.backend.domain.report.entity.ReportStatus;
import com.uniroad.backend.domain.report.repository.ReportRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createReport(Long reporterId, ReportRequest request) {
        Member reporter = getMember(reporterId);
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .detail(request.detail())
                .status(ReportStatus.PENDING)
                .build();
        return reportRepository.save(report).getId();
    }

    public Page<ReportResponse> getReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(ReportResponse::from);
    }

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream().map(ReportResponse::from).toList();
    }

    @Transactional
    public ReportResponse updateReportStatus(Long id, AdminReportUpdateRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        report.updateStatus(request.status(), request.adminMemo());
        return ReportResponse.from(report);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public long countAll() {
        return reportRepository.count();
    }

    public long countResolved() {
        return reportRepository.countByStatus(ReportStatus.RESOLVED);
    }

    public long countPending() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }
}
