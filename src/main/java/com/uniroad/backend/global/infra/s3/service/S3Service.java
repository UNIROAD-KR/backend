package com.uniroad.backend.global.infra.s3.service;

import com.uniroad.backend.global.infra.s3.dto.PresignedUrlRequestDto;
import com.uniroad.backend.global.infra.s3.dto.PresignedUrlResponseDto;
import com.uniroad.backend.global.infra.s3.entity.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public PresignedUrlResponseDto getPresignedUrl(PresignedUrlRequestDto requestDto) {
        FileType fileType = FileType.valueOf(requestDto.getFileType());

        validateContentType(fileType, requestDto.getContentType());

        String folder = switch (fileType) {
            case IMAGE -> "images";
            case PDF -> "pdfs";
        };

        String key = folder + "/" + UUID.randomUUID() + "_" + requestDto.getFileName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(requestDto.getContentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        String fileUrl =
                "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

        return PresignedUrlResponseDto.builder()
                .uploadUrl(presignedRequest.url().toString())
                .fileUrl(fileUrl)
                .key(key)
                .build();
    }

    private void validateContentType(FileType fileType, String contentType) {
        switch (fileType) {
            case IMAGE -> {
                if (!contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
                }
            }
            case PDF -> {
                if (!contentType.equals("application/pdf")) {
                    throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
                }
            }
        }
    }
}