package com.uniroad.backend.global.infra.s3.service;

import com.uniroad.backend.global.infra.s3.dto.PresignedUrlRequestDto;
import com.uniroad.backend.global.infra.s3.dto.PresignedUrlResponseDto;
import com.uniroad.backend.global.infra.s3.dto.PrivatePresignedUrlRequestDto;
import com.uniroad.backend.global.infra.s3.dto.PrivatePresignedUrlResponseDto;
import com.uniroad.backend.global.infra.s3.entity.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Duration UPLOAD_URL_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration DOWNLOAD_URL_EXPIRATION = Duration.ofMinutes(10);
    private static final String PRIVATE_EXCHANGE_VERIFICATION_PREFIX = "private/exchange-verifications/";

    /** S3가 한 번의 삭제 요청으로 받는 key 개수 상한 */
    private static final int DELETE_BATCH_SIZE = 1000;

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

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

    /**
     * 서버가 정한 key로 올리는 업로드 URL.
     *
     * key를 호출하는 쪽이 만들기 때문에, 누구의 어떤 파일인지 key 경로로 검증할 수 있다.
     */
    public String createUploadUrl(String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        return presignPutObject(putObjectRequest).url().toString();
    }

    /**
     * 비공개 객체를 일정 시간 동안 읽을 수 있는 URL.
     *
     * 서명은 네트워크를 타지 않고 로컬에서 계산하므로 목록 조회에서 여러 장을 한꺼번에 만들어도 된다.
     * 이 key를 읽어도 되는 사람인지는 호출하는 쪽이 먼저 확인해야 한다.
     */
    public String createReadUrl(String key, Duration expiration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public void deleteObjects(Collection<String> keys) {
        List<ObjectIdentifier> identifiers = keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        for (int from = 0; from < identifiers.size(); from += DELETE_BATCH_SIZE) {
            List<ObjectIdentifier> batch = new ArrayList<>(
                    identifiers.subList(from, Math.min(from + DELETE_BATCH_SIZE, identifiers.size())));

            s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(batch).quiet(true).build())
                    .build());
        }
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
