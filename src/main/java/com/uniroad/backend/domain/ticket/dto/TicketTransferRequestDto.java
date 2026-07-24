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

    private String customTicketType;

    @NotBlank
    private String title;

    private String content;

    private String country;

    private String useDate;
    private String useTime;
    private String placeName;

    private String performanceDate;
    private String performanceTime;
    private String performancePlace;

    private String departureDate;
    private String departureTime;
    private String departureStation;
    private String arrivalStation;

    private String departureAirport;
    private String arrivalAirport;

    private String checkInDate;
    private String checkOutDate;
    private String accommodationName;

    @NotNull
    private Integer quantity;

    @NotNull
    private Long transferPrice;

    private Long originalPrice;
}
