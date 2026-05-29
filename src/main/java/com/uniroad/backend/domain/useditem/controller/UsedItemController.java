package com.uniroad.backend.domain.useditem.controller;

import com.uniroad.backend.domain.useditem.dto.UsedItemRequestDto;
import com.uniroad.backend.domain.useditem.dto.UsedItemResponseDto;
import com.uniroad.backend.domain.useditem.dto.UsedItemSummaryResponseDto;
import com.uniroad.backend.domain.useditem.service.UsedItemService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.common.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "UsedItem", description = "중고거래 API")
@RestController
@RequestMapping("/api/used-items")
@RequiredArgsConstructor
public class UsedItemController {

    private final UsedItemService usedItemService;

    @Operation(summary = "중고거래 게시글 작성", description = "인증된(VERIFIED) 또는 관리자(ADMIN)만 작성할 수 있습니다.")
    @PostMapping
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createUsedItem(@Valid @RequestBody UsedItemRequestDto requestDto) {
        Long usedItemId = usedItemService.createUsedItem(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "중고거래 게시글이 등록되었습니다.", usedItemId));
    }

    @Operation(
            summary = "중고거래 게시글 목록 조회",
            description = "로그인 사용자의 경우 소속 지역 게시글을 우선 정렬하고, 이후 최신순으로 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<UsedItemSummaryResponseDto>>> getUsedItems(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {

        CursorPageResponse<UsedItemSummaryResponseDto> response =
                usedItemService.getUsedItems(cursorId, size);

        return ResponseEntity.ok(
                ApiResponse.success("중고거래 게시글 목록 조회 성공", response)
        );
    }

    @Operation(summary = "중고거래 게시글 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsedItemResponseDto>> getUsedItem(@PathVariable Long id) {
        UsedItemResponseDto response = usedItemService.getUsedItem(id);
        return ResponseEntity.ok(ApiResponse.success("중고거래 게시글 상세 조회 성공", response));
    }

    @Operation(summary = "중고거래 게시글 삭제", description = "작성자 본인 또는 관리자만 삭제할 수 있습니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUsedItem(@PathVariable Long id) {
        usedItemService.deleteUsedItem(id);
        return ResponseEntity.ok(ApiResponse.success("중고거래 게시글이 삭제되었습니다.", null));
    }
}
