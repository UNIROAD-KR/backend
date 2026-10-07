package com.uniroad.backend.domain.ticket.dto;

import com.uniroad.backend.domain.ticket.entity.TicketTransferStatus;
import com.uniroad.backend.global.common.SortOrder;

import java.time.LocalDate;

public record TicketTransferSearchRequest(
        String title,
        String country,
        String location,
        String content,
        TicketTransferStatus status,
        Long minPrice,
        Long maxPrice,
        LocalDate useDateTo,
        SortOrder sort
) {
}
