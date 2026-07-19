package com.uniroad.backend.domain.report.repository;

import com.uniroad.backend.domain.report.entity.Report;
import com.uniroad.backend.domain.report.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByStatus(ReportStatus status);
}
