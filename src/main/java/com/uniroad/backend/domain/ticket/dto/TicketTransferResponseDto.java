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
    private LocalDate authorDispatchStartDate;
    private TicketType ticketType;
    private String title;
    private String content;
    private String country;
    private String eventDate;
    private String eventEndDate;
    private String eventTime;
    private String location;
    private Integer quantity;
    private Long transferPrice;
    private Long originalPrice;
    private TicketTransferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketTransferResponseDto from(TicketTransferPost post) {
        return TicketTransferResponseDto.builder()
                .id(post.getId())
                .authorName(post.getAuthor().getName())
                .authorNickname(post.getAuthor().getNickname())
                .authorDispatchedCountry(post.getAuthor().getDispatchedCountry())
                .authorDispatchedRegion(post.getAuthor().getDispatchedRegion())
                .authorDispatchedUniversity(post.getAuthor().getDispatchedUniversity())
                .authorDispatchStartDate(post.getAuthor().getDispatchStartDate())
                .ticketType(post.getTicketType())
                .title(post.getTitle())
                .content(post.getContent())
                .country(post.getCountry())
                .eventDate(post.getEventDate())
                .eventEndDate(post.getEventEndDate())
                .eventTime(post.getEventTime())
                .location(post.getLocation())
                .quantity(post.getQuantity())
                .transferPrice(post.getTransferPrice())
                .originalPrice(post.getOriginalPrice())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
