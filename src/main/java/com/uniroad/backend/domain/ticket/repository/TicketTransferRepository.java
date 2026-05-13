package com.uniroad.backend.domain.ticket.repository;

import com.uniroad.backend.domain.ticket.entity.TicketTransferPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTransferRepository extends JpaRepository<TicketTransferPost, Long> {
}