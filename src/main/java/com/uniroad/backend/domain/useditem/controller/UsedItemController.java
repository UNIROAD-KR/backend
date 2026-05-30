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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UsedItem", description = "중고거래 API")
@RestController
@RequestMapping("/api/used-items")
@RequiredArgsConstructor
public class UsedItemController {

    private final UsedItemService usedItemService;

    @Operation(summary = "중고거래 게시글 작성", description = "인증 회원 또는 관리자만 작성할 수 있습니다.")
    @PostMapping
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createUsedItem(@Valid @RequestBody UsedItemRequestDto requestDto) {
        Long usedItemId = usedItemService.createUsedItem(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "중고거래 게시글 등록 성공", usedItemId));
    }

    @Operation(summary = "중고거래 게시글 목록 조회")
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

    @Operation(summary = "내 중고거래 글 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<CursorPageResponse<UsedItemSummaryResponseDto>>> getMyUsedItems(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        CursorPageResponse<UsedItemSummaryResponseDto> response =
                usedItemService.getMyUsedItems(cursorId, size);

        return ResponseEntity.ok(
                ApiResponse.success("내 중고거래 글 조회 성공", response)
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
        return ResponseEntity.ok(ApiResponse.success("중고거래 게시글 삭제 성공", null));
    }
}
