package com.uniroad.backend.global.infra.s3.service;

import com.uniroad.backend.global.infra.s3.dto.PresignedUrlRequestDto;
import com.uniroad.backend.global.infra.s3.dto.PresignedUrlResponseDto;
import com.uniroad.backend.global.infra.s3.dto.PrivatePresignedUrlRequestDto;
import com.uniroad.backend.global.infra.s3.dto.PrivatePresignedUrlResponseDto;
import com.uniroad.backend.global.infra.s3.entity.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Duration UPLOAD_URL_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration DOWNLOAD_URL_EXPIRATION = Duration.ofMinutes(10);
    private static final String PRIVATE_EXCHANGE_VERIFICATION_PREFIX = "private/exchange-verifications/";

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

        PresignedPutObjectRequest presignedRequest = presignPutObject(putObjectRequest);

        String fileUrl =
                "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

        return PresignedUrlResponseDto.builder()
                .uploadUrl(presignedRequest.url().toString())
                .fileUrl(fileUrl)
                .key(key)
                .build();
    }

    public PresignedUrlResponseDto getExchangeVerificationUploadUrl(PresignedUrlRequestDto requestDto) {
        FileType fileType = FileType.valueOf(requestDto.getFileType());

        validateContentType(fileType, requestDto.getContentType());

        String folder = switch (fileType) {
            case IMAGE -> "images";
            case PDF -> "pdfs";
        };

        String key = PRIVATE_EXCHANGE_VERIFICATION_PREFIX
                + folder
                + "/"
                + UUID.randomUUID()
                + "_"
                + requestDto.getFileName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(requestDto.getContentType())
                .build();

        PresignedPutObjectRequest presignedRequest = presignPutObject(putObjectRequest);

        return PresignedUrlResponseDto.builder()
                .uploadUrl(presignedRequest.url().toString())
                .key(key)
                .build();
    }

    public PrivatePresignedUrlResponseDto getExchangeVerificationReadUrl(PrivatePresignedUrlRequestDto requestDto) {
        String key = requestDto.getKey();
        validatePrivateExchangeVerificationKey(key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(DOWNLOAD_URL_EXPIRATION)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return PrivatePresignedUrlResponseDto.builder()
                .downloadUrl(presignedRequest.url().toString())
                .build();
    }

    private PresignedPutObjectRequest presignPutObject(PutObjectRequest putObjectRequest) {
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest);
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

    private void validatePrivateExchangeVerificationKey(String key) {
        if (!key.startsWith(PRIVATE_EXCHANGE_VERIFICATION_PREFIX)) {
            throw new IllegalArgumentException("교환학생 인증 파일만 조회 가능합니다.");
        }
    }
}
