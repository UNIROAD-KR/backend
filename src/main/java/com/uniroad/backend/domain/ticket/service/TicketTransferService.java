package com.uniroad.backend.domain.ticket.service;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.ticket.dto.TicketTransferRequestDto;
import com.uniroad.backend.domain.ticket.dto.TicketTransferResponseDto;
import com.uniroad.backend.domain.ticket.entity.TicketTransferPost;
import com.uniroad.backend.domain.ticket.repository.TicketTransferRepository;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import com.uniroad.backend.domain.scrap.repository.ScrapRepository;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTransferService {

    private final TicketTransferRepository ticketTransferRepository;
    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(TicketTransferRequestDto requestDto) {
        Member member = getCurrentMember();

        TicketTransferPost post = TicketTransferPost.builder()
                .author(member)
                .ticketType(requestDto.getTicketType())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .country(requestDto.getCountry())
                .eventDate(requestDto.getEventDate())
                .eventEndDate(requestDto.getEventEndDate())
                .eventTime(requestDto.getEventTime())
                .location(requestDto.getLocation())
                .quantity(requestDto.getQuantity())
                .transferPrice(requestDto.getTransferPrice())
                .originalPrice(requestDto.getOriginalPrice())
                .build();

        return ticketTransferRepository.save(post).getId();
    }

    public TicketTransferResponseDto getDetail(Long id) {
        TicketTransferPost post = getPost(id);
        return TicketTransferResponseDto.from(post, scrapRepository.countByTargetTypeAndTargetId(ScrapTargetType.TICKET_TRANSFER, id));
    }

    public CursorPageResponse<TicketTransferResponseDto> getTickets(Long cursorId, int size) {
        int requestSize = normalizeSize(size);
        List<TicketTransferPost> posts = ticketTransferRepository.findByCursor(
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );

        return toCursorResponse(posts, requestSize);
    }

    public CursorPageResponse<TicketTransferResponseDto> getMyTickets(Long cursorId, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        int requestSize = normalizeSize(size);
        List<TicketTransferPost> posts = ticketTransferRepository.findByAuthorIdAndCursor(
                memberId,
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );

        return toCursorResponse(posts, requestSize);
    }

    public CursorPageResponse<TicketTransferResponseDto> getMyScrappedTickets(Long cursorId, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        int requestSize = normalizeSize(size);
        List<TicketTransferPost> posts = ticketTransferRepository.findScrappedByMemberIdAndCursor(
                memberId,
                ScrapTargetType.TICKET_TRANSFER,
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );
        return toCursorResponse(posts, requestSize);
    }

    @Transactional
    public void update(Long id, TicketTransferRequestDto requestDto) {
        Member member = getCurrentMember();
        TicketTransferPost post = getPost(id);

        validateOwnership(member, post);

        post.update(
                requestDto.getTicketType(),
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getCountry(),
                requestDto.getEventDate(),
                requestDto.getEventEndDate(),
                requestDto.getEventTime(),
                requestDto.getLocation(),
                requestDto.getQuantity(),
                requestDto.getTransferPrice(),
                requestDto.getOriginalPrice()
        );
    }

    @Transactional
    public void delete(Long id) {
        Member member = getCurrentMember();
        TicketTransferPost post = getPost(id);

        validateOwnership(member, post);

        scrapRepository.deleteAllByTargetTypeAndTargetId(ScrapTargetType.TICKET_TRANSFER, id);
        ticketTransferRepository.delete(post);
    }

    @Transactional
    public void complete(Long id) {
        Member member = getCurrentMember();
        TicketTransferPost post = getPost(id);

        validateOwnership(member, post);

        post.markCompleted();
    }

    private Member getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() == Role.USER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return member;
    }

    private TicketTransferPost getPost(Long id) {
        return ticketTransferRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateOwnership(Member member, TicketTransferPost post) {
        if (member.getRole() == Role.ADMIN) {
            return;
        }

        if (!post.getAuthor().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private CursorPageResponse<TicketTransferResponseDto> toCursorResponse(List<TicketTransferPost> posts, int requestSize) {
        boolean hasNext = posts.size() > requestSize;
        List<TicketTransferPost> pagePosts = hasNext ? posts.subList(0, requestSize) : posts;
        List<TicketTransferResponseDto> items = pagePosts.stream()
                .map(post -> TicketTransferResponseDto.from(
                        post,
                        scrapRepository.countByTargetTypeAndTargetId(ScrapTargetType.TICKET_TRANSFER, post.getId())
                ))
                .toList();

        Long nextCursorId = hasNext ? pagePosts.get(pagePosts.size() - 1).getId() : null;
        return new CursorPageResponse<>(items, nextCursorId, hasNext);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
