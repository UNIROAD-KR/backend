package com.uniroad.backend.global.infra.s3.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedUrlResponseDto {

    private String uploadUrl;
    private String fileUrl;
    private String key;
}