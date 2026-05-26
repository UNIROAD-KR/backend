package com.uniroad.backend.global.infra.s3.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrivatePresignedUrlResponseDto {

    private String downloadUrl;
}
