package com.uniroad.backend.domain.chat.dto;

/** 채팅방이 가리키는 게시글의 제목과 대표 이미지. 이미지가 없는 게시판이면 imageUrl은 null이다. */
public record ChatReferenceSummary(String title, String imageUrl) {
}
