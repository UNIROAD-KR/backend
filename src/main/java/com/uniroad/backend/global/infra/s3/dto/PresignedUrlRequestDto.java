package com.uniroad.backend.global.infra.s3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PresignedUrlRequestDto {

    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;

    @NotBlank
    private String fileType; // IMAGE / PDF
}