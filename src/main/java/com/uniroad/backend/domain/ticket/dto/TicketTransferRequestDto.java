package com.uniroad.backend.domain.ticket.dto;

import com.uniroad.backend.domain.ticket.entity.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTransferRequestDto {

    @NotNull
    private TicketType ticketType;

    @NotBlank
    private String title;

    private String content;

    @NotBlank
    private String country;

    @NotBlank
    private String eventDate;

    private String eventEndDate;

    @NotBlank
    private String eventTime;

    @NotBlank
    private String location;

    @NotNull
    private Integer quantity;

    @NotNull
    private Long transferPrice;

    private Long originalPrice;
}
