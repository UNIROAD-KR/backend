package com.uniroad.backend.domain.ticket.controller;

import com.uniroad.backend.domain.ticket.dto.TicketTransferRequestDto;
import com.uniroad.backend.domain.ticket.dto.TicketTransferResponseDto;
import com.uniroad.backend.domain.ticket.service.TicketTransferService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ticket Transfer", description = "티켓 양도 API")
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketTransferController {

    private final TicketTransferService ticketTransferService;

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

    @Operation(summary = "티켓 양도 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketTransferResponseDto>> getDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "티켓 양도 상세 조회 성공",
                        ticketTransferService.getDetail(id)
                )
        );
    }

    @Operation(summary = "티켓 양도 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody TicketTransferRequestDto requestDto
    ) {
        ticketTransferService.update(id, requestDto);

        return ResponseEntity.ok(
                ApiResponse.success("티켓 양도 수정 성공", null)
        );
    }

    @Operation(summary = "티켓 양도 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {
        ticketTransferService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("티켓 양도 삭제 성공", null)
        );
    }

    @Operation(summary = "판매 완료 처리")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> complete(
            @PathVariable Long id
    ) {
        ticketTransferService.complete(id);

        return ResponseEntity.ok(
                ApiResponse.success("판매 완료 처리 성공", null)
        );
    }
}