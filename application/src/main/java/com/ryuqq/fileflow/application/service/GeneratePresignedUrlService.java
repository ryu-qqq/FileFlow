package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;
import com.ryuqq.fileflow.application.port.in.command.GeneratePresignedUrlUseCase;
import com.ryuqq.fileflow.application.port.out.command.FilePersistencePort;
import com.ryuqq.fileflow.application.port.out.external.S3ClientPort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.UploaderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

/**
 * Presigned URL 생성 UseCase 구현
 * <p>
 * Zero-Tolerance 규칙:
 * - @Transactional 내 외부 API 호출 절대 금지
 * - Transaction 경계: 파일 메타데이터 저장만 트랜잭션 내부
 * - S3 API 호출: 트랜잭션 외부에서 실행
 * </p>
 */
@Service
public class GeneratePresignedUrlService implements GeneratePresignedUrlUseCase {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1); // 1시간
    private static final String S3_BUCKET = "fileflow-bucket"; // TODO: 설정 외부화
    private static final long MULTIPART_THRESHOLD = 100L * 1024L * 1024L; // 100MB

    private final FilePersistencePort filePersistencePort;
    private final S3ClientPort s3ClientPort;
    private final Clock clock;

    public GeneratePresignedUrlService(
            FilePersistencePort filePersistencePort,
            S3ClientPort s3ClientPort,
            Clock clock
    ) {
        this.filePersistencePort = Objects.requireNonNull(filePersistencePort, "filePersistencePort must not be null");
        this.s3ClientPort = Objects.requireNonNull(s3ClientPort, "s3ClientPort must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * Presigned URL 생성 UseCase 실행
     * <p>
     * 워크플로우:
     * 1. @Transactional 내부: 파일 메타데이터 저장 (DB)
     * 2. 트랜잭션 커밋 후: S3 Presigned URL 생성 (외부 API)
     * </p>
     */
    @Override
    public PresignedUrlResponse execute(GeneratePresignedUrlCommand command) {
        // 1. 트랜잭션 내부: 파일 메타데이터 저장
        FileId fileId = saveFileMetadata(command);

        // 2. 트랜잭션 외부: S3 Presigned URL 생성
        return generatePresignedUrlResponse(fileId, command);
    }

    /**
     * 파일 메타데이터 저장 (트랜잭션 내부)
     * <p>
     * @Transactional 경계 내에서 DB만 접근합니다.
     * 외부 API 호출 절대 금지!
     * </p>
     */
    @Transactional
    protected FileId saveFileMetadata(GeneratePresignedUrlCommand command) {
        // S3 키 생성 (업로드 경로)
        String s3Key = generateS3Key(command.fileName());

        // File Aggregate 생성
        File file = File.forNew(
                command.fileName(),
                command.fileSize(),
                command.mimeType(),
                s3Key,
                S3_BUCKET,
                UploaderId.of(command.uploaderId()),
                command.category(),
                String.join(",", command.tags()),
                clock
        );

        // 파일 메타데이터 영속화
        return filePersistencePort.persist(file);
    }

    /**
     * Presigned URL Response 생성 (트랜잭션 외부)
     * <p>
     * 트랜잭션 커밋 후 S3 외부 API를 호출합니다.
     * </p>
     */
    private PresignedUrlResponse generatePresignedUrlResponse(FileId fileId, GeneratePresignedUrlCommand command) {
        // S3 키 재생성 (동일한 로직)
        String s3Key = generateS3Key(command.fileName());

        // S3 Presigned URL 생성 (외부 API 호출)
        String presignedUrl = s3ClientPort.generatePresignedUrl(s3Key, PRESIGNED_URL_EXPIRATION);

        // 업로드 전략 결정 (100MB 기준)
        String uploadStrategy = determineUploadStrategy(command.fileSize());

        // Response 생성
        return new PresignedUrlResponse(
                (long) fileId.getValue().hashCode(), // TODO: UUID String -> Long 임시 변환, 설계 재검토 필요
                presignedUrl,
                PRESIGNED_URL_EXPIRATION.toSeconds(),
                s3Key,
                uploadStrategy
        );
    }

    /**
     * 업로드 전략 결정
     * <p>
     * 파일 크기에 따라 업로드 전략을 결정합니다:
     * - < 100MB: SINGLE (단일 PUT 요청)
     * - >= 100MB: MULTIPART (여러 Part로 분할 업로드)
     * </p>
     */
    private String determineUploadStrategy(long fileSize) {
        if (fileSize < MULTIPART_THRESHOLD) {
            return "SINGLE";
        } else {
            return "MULTIPART";
        }
    }

    /**
     * S3 키 생성
     * <p>
     * 경로 패턴: uploads/{year}/{month}/{day}/{uuid}-{fileName}
     * </p>
     */
    private String generateS3Key(String fileName) {
        // TODO: 더 정교한 S3 키 생성 로직 필요 (UUID, 날짜 경로 등)
        return "uploads/" + fileName;
    }
}
