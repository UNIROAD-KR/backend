package com.uniroad.backend.domain.companion.dto;

import com.uniroad.backend.domain.companion.entity.RecruitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "동행 구하기 게시글 작성/수정 요청")
public record CompanionPostRequest(
    @Schema(description = "제목", example = "파리 에펠탑 야경 보실 분!")
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @Schema(description = "내용", example = "혼자 가기 무서워서 같이 가실 분 구해요.")
    @NotBlank(message = "내용은 필수입니다.")
    String content,

    @Schema(description = "시작 날짜", example = "2024-06-01")
    @NotNull(message = "시작 날짜는 필수입니다.")
    LocalDate startDate,

    @Schema(description = "종료 날짜", example = "2024-06-05")
    @NotNull(message = "종료 날짜는 필수입니다.")
    LocalDate endDate,

    @Schema(description = "나라", example = "프랑스")
    @NotBlank(message = "나라는 필수입니다.")
    String country,

    @Schema(description = "지역", example = "파리")
    @NotBlank(message = "지역은 필수입니다.")
    String region,

    @Schema(description = "오픈채팅방 링크", example = "https://open.kakao.com/o/s123456")
    @NotBlank(message = "채팅 링크는 필수입니다.")
    String chatLink,

    @Schema(description = "모집 상태", example = "RECRUITING")
    @NotNull(message = "모집 상태는 필수입니다.")
    RecruitmentStatus status,

    @Schema(description = "정원", example = "4")
    @NotNull(message = "정원은 필수입니다.")
    @Positive(message = "정원은 양수여야 합니다.")
    Integer capacity,

    @Schema(description = "성비", example = "1:1")
    @NotBlank(message = "성비는 필수입니다.")
    String genderRatio
) {
}
