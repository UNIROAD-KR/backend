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

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String country;

    @Column(nullable = false)
    private String eventDate;

    private String eventEndDate;

    @Column(nullable = false)
    private String eventTime;

    @Column(nullable = false)
    private String location;

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
            String title,
            String content,
            String country,
            String eventDate,
            String eventEndDate,
            String eventTime,
            String location,
            Integer quantity,
            Long transferPrice,
            Long originalPrice
    ) {
        this.ticketType = ticketType;
        this.title = title;
        this.content = content;
        this.country = country;
        this.eventDate = eventDate;
        this.eventEndDate = eventEndDate;
        this.eventTime = eventTime;
        this.location = location;
        this.quantity = quantity;
        this.transferPrice = transferPrice;
        this.originalPrice = originalPrice;
    }
}
