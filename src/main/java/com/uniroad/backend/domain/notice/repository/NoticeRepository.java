package com.uniroad.backend.domain.notice.repository;

import com.uniroad.backend.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
