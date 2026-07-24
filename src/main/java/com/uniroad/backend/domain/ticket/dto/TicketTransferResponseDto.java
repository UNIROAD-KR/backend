package com.uniroad.backend.domain.ticket.dto;

import com.uniroad.backend.domain.ticket.entity.TicketTransferPost;
import com.uniroad.backend.domain.ticket.entity.TicketTransferStatus;
import com.uniroad.backend.domain.ticket.entity.TicketType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTransferResponseDto {

    private Long id;
    private String authorName;
    private String authorNickname;
    private String authorDispatchedCountry;
    private String authorDispatchedRegion;
    private String authorDispatchedUniversity;
    private Integer authorDispatchYear;
    private String authorDispatchSemester;
    private LocalDate authorDispatchStartDate;
    private TicketType ticketType;
    private String customTicketType;
    private String title;
    private String content;
    private String country;
    private long scrapCount;
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
    private Integer quantity;
    private Long transferPrice;
    private Long originalPrice;
    private TicketTransferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketTransferResponseDto from(TicketTransferPost post, long scrapCount) {
        return TicketTransferResponseDto.builder()
                .id(post.getId())
                .authorName(post.getAuthor().getName())
                .authorNickname(post.getAuthor().getNickname())
                .authorDispatchedCountry(post.getAuthor().getDispatchedCountry())
                .authorDispatchedRegion(post.getAuthor().getDispatchedRegion())
                .authorDispatchedUniversity(post.getAuthor().getDispatchedUniversity())
                .authorDispatchYear(post.getAuthor().getDispatchYear())
                .authorDispatchSemester(post.getAuthor().getDispatchSemester())
                .authorDispatchStartDate(post.getAuthor().getDispatchStartDate())
                .ticketType(post.getTicketType())
                .customTicketType(post.getCustomTicketType())
                .title(post.getTitle())
                .content(post.getContent())
                .country(post.getCountry())
                .scrapCount(scrapCount)
                .useDate(post.getUseDate())
                .useTime(post.getUseTime())
                .placeName(post.getPlaceName())
                .performanceDate(post.getPerformanceDate())
                .performanceTime(post.getPerformanceTime())
                .performancePlace(post.getPerformancePlace())
                .departureDate(post.getDepartureDate())
                .departureTime(post.getDepartureTime())
                .departureStation(post.getDepartureStation())
                .arrivalStation(post.getArrivalStation())
                .departureAirport(post.getDepartureAirport())
                .arrivalAirport(post.getArrivalAirport())
                .checkInDate(post.getCheckInDate())
                .checkOutDate(post.getCheckOutDate())
                .accommodationName(post.getAccommodationName())
                .quantity(post.getQuantity())
                .transferPrice(post.getTransferPrice())
                .originalPrice(post.getOriginalPrice())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
