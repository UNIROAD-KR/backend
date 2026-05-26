package com.uniroad.backend.global.infra.s3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PrivatePresignedUrlRequestDto {

    @NotBlank
    private String key;
}
