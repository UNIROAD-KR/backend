package com.uniroad.backend.domain.ticket.dto;

import com.uniroad.backend.domain.ticket.entity.TicketTransferStatus;

public record TicketTransferSearchRequest(
        String title,
        String country,
        String location,
        String content,
        TicketTransferStatus status
) {
}
