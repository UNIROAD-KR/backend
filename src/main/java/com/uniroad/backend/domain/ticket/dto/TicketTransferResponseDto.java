package com.uniroad.backend.domain.ticket.dto;

import com.uniroad.backend.domain.ticket.entity.TicketTransferPost;
import com.uniroad.backend.domain.ticket.entity.TicketTransferStatus;
import com.uniroad.backend.domain.ticket.entity.TicketType;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTransferResponseDto {

    private Long id;
    private String authorName;
    private TicketType ticketType;
    private String title;
    private String content;
    private String eventDate;
    private String eventTime;
    private String location;
    private Integer quantity;
    private Long transferPrice;
    private Long originalPrice;
    private TicketTransferStatus status;

    public static TicketTransferResponseDto from(TicketTransferPost post) {
        return TicketTransferResponseDto.builder()
                .id(post.getId())
                .authorName(post.getAuthor().getName())
                .ticketType(post.getTicketType())
                .title(post.getTitle())
                .content(post.getContent())
                .eventDate(post.getEventDate())
                .eventTime(post.getEventTime())
                .location(post.getLocation())
                .quantity(post.getQuantity())
                .transferPrice(post.getTransferPrice())
                .originalPrice(post.getOriginalPrice())
                .status(post.getStatus())
                .build();
    }
}