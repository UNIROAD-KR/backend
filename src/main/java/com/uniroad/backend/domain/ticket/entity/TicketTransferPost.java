package com.uniroad.backend.domain.ticket.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TicketTransferPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    // 티켓 종류
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    // 제목
    @Column(nullable = false, length = 100)
    private String title;

    // 상세 설명
    @Column(columnDefinition = "TEXT")
    private String content;

    // 날짜
    @Column(nullable = false)
    private String eventDate;

    // 시간
    @Column(nullable = false)
    private String eventTime;

    // 장소
    @Column(nullable = false)
    private String location;

    // 양도 매수
    @Column(nullable = false)
    private Integer quantity;

    // 양도 가격
    @Column(nullable = false)
    private Long transferPrice;

    // 원가
    private Long originalPrice;

    // 상태
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
            String eventDate,
            String eventTime,
            String location,
            Integer quantity,
            Long transferPrice,
            Long originalPrice
    ) {
        this.ticketType = ticketType;
        this.title = title;
        this.content = content;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.quantity = quantity;
        this.transferPrice = transferPrice;
        this.originalPrice = originalPrice;
    }
}