package com.uniroad.backend.domain.ticket.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TicketTransferPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    @Column(length = 100)
    private String customTicketType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
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

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long transferPrice;

    private Long originalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketTransferStatus status = TicketTransferStatus.AVAILABLE;

    public void markCompleted() {
        this.status = TicketTransferStatus.COMPLETED;
    }

    public void update(
            TicketType ticketType,
            String customTicketType,
            String title,
            String content,
            String country,
            String useDate,
            String useTime,
            String placeName,
            String performanceDate,
            String performanceTime,
            String performancePlace,
            String departureDate,
            String departureTime,
            String departureStation,
            String arrivalStation,
            String departureAirport,
            String arrivalAirport,
            String checkInDate,
            String checkOutDate,
            String accommodationName,
            Integer quantity,
            Long transferPrice,
            Long originalPrice
    ) {
        this.ticketType = ticketType;
        this.customTicketType = customTicketType;
        this.title = title;
        this.content = content;
        this.country = country;
        this.useDate = useDate;
        this.useTime = useTime;
        this.placeName = placeName;
        this.performanceDate = performanceDate;
        this.performanceTime = performanceTime;
        this.performancePlace = performancePlace;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.accommodationName = accommodationName;
        this.quantity = quantity;
        this.transferPrice = transferPrice;
        this.originalPrice = originalPrice;
    }
}
