package com.ryuqq.fileflow.adapter.s3.adapter;

import com.ryuqq.fileflow.adapter.s3.config.S3Properties;
import com.ryuqq.fileflow.application.upload.port.out.GeneratePresignedUrlPort;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.vo.PresignedUrlInfo;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * AWS S3 Presigned URL 생성 Adapter
 * GeneratePresignedUrlPort를 구현하여 S3 업로드용 Presigned URL을 생성합니다.
 *
 * Hexagonal Architecture:
 * - Adapter Layer의 구현체 (Outbound Adapter)
 * - Application Layer의 Port 인터페이스를 구현
 * - AWS S3 SDK를 사용한 외부 인프라 연동
 *
 * 보안 설정:
 * - 서명된 URL 생성 (AWS Signature V4)
 * - 설정 가능한 만료 시간 (기본 15분)
 * - HTTPS 강제 사용
 * - Content-Type 지정으로 파일 타입 제한
 * - 파일 크기 제한 (Content-Length)
 *
 * NO Lombok:
 * - 명시적인 생성자와 메서드 사용
 */
@Component
public class S3PresignedUrlAdapter implements GeneratePresignedUrlPort {

    private static final String METADATA_UPLOADER_ID = "x-amz-meta-uploader-id";
    private static final String METADATA_ORIGINAL_FILENAME = "x-amz-meta-original-filename";
    private static final String METADATA_FILE_TYPE = "x-amz-meta-file-type";

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    /**
     * S3PresignedUrlAdapter 생성자
     *
     * @param s3Presigner AWS S3 Presigner
     * @param s3Properties S3 설정 프로퍼티
     * @throws IllegalArgumentException s3Presigner 또는 s3Properties가 null인 경우
     */
    public S3PresignedUrlAdapter(
            S3Presigner s3Presigner,
            S3Properties s3Properties
    ) {
        if (s3Presigner == null) {
            throw new IllegalArgumentException("S3Presigner cannot be null");
        }
        if (s3Properties == null) {
            throw new IllegalArgumentException("S3Properties cannot be null");
        }

        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
    }

    /**
     * 파일 업로드를 위한 Presigned URL을 생성합니다.
     *
     * URL 생성 프로세스:
     * 1. 업로드 경로 생성 (pathPrefix/uploaderId/UUID/filename)
     * 2. PutObjectRequest 생성 (Content-Type, Content-Length, Metadata 포함)
     * 3. Presigned URL 생성 (설정된 만료 시간)
     * 4. PresignedUrlInfo 도메인 객체로 변환
     *
     * @param command 파일 업로드 명령
     * @return Presigned URL 정보
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws RuntimeException URL 생성 실패 시
     */
    @Override
    public PresignedUrlInfo generate(FileUploadCommand command) {
        validateCommand(command);

        String uploadPath = buildUploadPath(command);
        PutObjectRequest putObjectRequest = buildPutObjectRequest(uploadPath, command);
        PresignedPutObjectRequest presignedRequest = generatePresignedRequest(putObjectRequest);

        return convertToPresignedUrlInfo(presignedRequest, uploadPath);
    }

    // 형식: {pathPrefix}/{uploaderId}/{UUID}/{fileName}
    private String buildUploadPath(FileUploadCommand command) {
        String prefix = s3Properties.getPathPrefix();
        String uploaderId = command.uploaderId();
        String uniqueId = UUID.randomUUID().toString();
        String fileName = command.fileName();

        if (prefix.isEmpty()) {
            return String.format("%s/%s/%s", uploaderId, uniqueId, fileName);
        }

        return String.format("%s/%s/%s/%s", prefix, uploaderId, uniqueId, fileName);
    }

    private PutObjectRequest buildPutObjectRequest(String uploadPath, FileUploadCommand command) {
        return PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(uploadPath)
                .contentType(command.contentType())
                .contentLength(command.fileSizeBytes())
                .metadata(java.util.Map.of(
                        METADATA_UPLOADER_ID, command.uploaderId(),
                        METADATA_ORIGINAL_FILENAME, command.fileName(),
                        METADATA_FILE_TYPE, command.fileType().name()
                ))
                .build();
    }

    private PresignedPutObjectRequest generatePresignedRequest(PutObjectRequest putObjectRequest) {
        Duration signatureDuration = Duration.ofMinutes(s3Properties.getPresignedUrlExpirationMinutes());

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(signatureDuration)
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest);
    }

    private PresignedUrlInfo convertToPresignedUrlInfo(
            PresignedPutObjectRequest presignedRequest,
            String uploadPath
    ) {
        String presignedUrl = presignedRequest.url().toString();
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                presignedRequest.expiration(),
                ZoneId.systemDefault()
        );

        return PresignedUrlInfo.of(presignedUrl, uploadPath, expiresAt);
    }

    private static void validateCommand(FileUploadCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("FileUploadCommand cannot be null");
        }
    }
}
