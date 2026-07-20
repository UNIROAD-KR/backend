package com.uniroad.backend.domain.ticket.controller;

import com.uniroad.backend.domain.ticket.dto.TicketTransferRequestDto;
import com.uniroad.backend.domain.ticket.dto.TicketTransferResponseDto;
import com.uniroad.backend.domain.ticket.service.TicketTransferService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import com.uniroad.backend.domain.scrap.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ticket Transfer", description = "티켓 양도 API")
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketTransferController {

    private final TicketTransferService ticketTransferService;
    private final ScrapService scrapService;

    @Operation(summary = "티켓 양도 글 작성")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody TicketTransferRequestDto requestDto
    ) {
        Long id = ticketTransferService.create(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "티켓 양도 글 작성 성공",
                        id
                ));
    }

    @Operation(summary = "티켓 양도 글 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<TicketTransferResponseDto>>> getTickets(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "티켓 양도 글 목록 조회 성공",
                        ticketTransferService.getTickets(cursorId, size)
                )
        );
    }

    @Operation(summary = "내 티켓 양도 글 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<CursorPageResponse<TicketTransferResponseDto>>> getMyTickets(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "내 티켓 양도 글 조회 성공",
                        ticketTransferService.getMyTickets(cursorId, size)
                )
        );
    }

    @Operation(summary = "내가 스크랩한 티켓 양도 글 조회")
    @GetMapping("/scraps")
    public ResponseEntity<ApiResponse<CursorPageResponse<TicketTransferResponseDto>>> getMyScrappedTickets(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "내가 스크랩한 티켓 양도 글 조회 성공",
                        ticketTransferService.getMyScrappedTickets(cursorId, size)
                )
        );
    }

    @Operation(summary = "티켓 양도 글 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketTransferResponseDto>> getDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "티켓 양도 글 상세 조회 성공",
                        ticketTransferService.getDetail(id)
                )
        );
    }

    @Operation(summary = "티켓 양도 스크랩 토글")
    @PostMapping("/{id}/scrap")
    public ResponseEntity<ApiResponse<Boolean>> toggleScrap(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("티켓 양도 스크랩 토글 성공", scrapService.toggle(ScrapTargetType.TICKET_TRANSFER, id))
        );
    }

    @Operation(summary = "티켓 양도 글 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody TicketTransferRequestDto requestDto
    ) {
        ticketTransferService.update(id, requestDto);

        return ResponseEntity.ok(
                ApiResponse.success("티켓 양도 글 수정 성공", null)
        );
    }

    @Operation(summary = "티켓 양도 글 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {
        ticketTransferService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("티켓 양도 글 삭제 성공", null)
        );
    }

    @Operation(summary = "티켓 양도 완료 처리")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> complete(
            @PathVariable Long id
    ) {
        ticketTransferService.complete(id);

        return ResponseEntity.ok(
                ApiResponse.success("티켓 양도 완료 처리 성공", null)
        );
    }
}
