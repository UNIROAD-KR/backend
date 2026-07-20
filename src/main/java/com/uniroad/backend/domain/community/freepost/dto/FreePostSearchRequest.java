package com.uniroad.backend.domain.community.freepost.dto;

public record FreePostSearchRequest(
        String title,
        String content
) {
}
