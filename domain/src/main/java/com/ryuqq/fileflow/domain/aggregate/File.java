package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.exception.InvalidFileSizeException;
import com.ryuqq.fileflow.domain.exception.InvalidMimeTypeException;
import com.ryuqq.fileflow.domain.util.UuidV7Generator;
import com.ryuqq.fileflow.domain.vo.FileStatus;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 파일 Aggregate Root
 * <p>
 * 파일 메타데이터와 S3 저장 정보를 관리하는 도메인 엔티티입니다.
 * </p>
 */
public class File {

    /**
     * 최대 파일 크기: 1GB
     */
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L; // 1GB

    /**
     * CDN URL 베이스 경로
     */
    private static final String CDN_BASE_URL = "https://cdn.fileflow.com/";

    /**
     * 허용되는 MIME 타입 목록
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // 이미지
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            // 문서
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            // 엑셀
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            // HTML
            "text/html",
            "text/plain",
            "text/csv"
    );

    private final String fileId;
    private final String fileName;
    private final long fileSize;
    private final String mimeType;
    private final FileStatus status;
    private final String s3Key;
    private final String s3Bucket;
    private final String cdnUrl;
    private final Long uploaderId;
    private final String category;
    private final String tags;
    private final int version;
    private final LocalDateTime deletedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * File Aggregate 생성자
     *
     * @param fileId     파일 고유 ID (UUID v7)
     * @param fileName   파일명
     * @param fileSize   파일 크기 (바이트)
     * @param mimeType   MIME 타입
     * @param status     파일 상태
     * @param s3Key      S3 객체 키
     * @param s3Bucket   S3 버킷명
     * @param cdnUrl     CDN URL
     * @param uploaderId 업로더 사용자 ID (Long FK 전략)
     * @param category   파일 카테고리
     * @param tags       태그 (콤마 구분)
     * @param version    낙관적 락 버전
     * @param deletedAt  소프트 삭제 시각
     * @param createdAt  생성 시각
     * @param updatedAt  수정 시각
     */
    public File(
            String fileId,
            String fileName,
            long fileSize,
            String mimeType,
            FileStatus status,
            String s3Key,
            String s3Bucket,
            String cdnUrl,
            Long uploaderId,
            String category,
            String tags,
            int version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.status = status;
        this.s3Key = s3Key;
        this.s3Bucket = s3Bucket;
        this.cdnUrl = cdnUrl;
        this.uploaderId = uploaderId;
        this.category = category;
        this.tags = tags;
        this.version = version;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 파일 고유 ID 조회
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * 파일명 조회
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 파일 크기 조회 (바이트)
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * MIME 타입 조회
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 파일 상태 조회
     */
    public FileStatus getStatus() {
        return status;
    }

    /**
     * S3 객체 키 조회
     */
    public String getS3Key() {
        return s3Key;
    }

    /**
     * S3 버킷명 조회
     */
    public String getS3Bucket() {
        return s3Bucket;
    }

    /**
     * CDN URL 조회
     */
    public String getCdnUrl() {
        return cdnUrl;
    }

    /**
     * 업로더 사용자 ID 조회 (Long FK 전략)
     */
    public Long getUploaderId() {
        return uploaderId;
    }

    /**
     * 파일 카테고리 조회
     */
    public String getCategory() {
        return category;
    }

    /**
     * 태그 조회 (콤마 구분)
     */
    public String getTags() {
        return tags;
    }

    /**
     * 낙관적 락 버전 조회
     */
    public int getVersion() {
        return version;
    }

    /**
     * 소프트 삭제 시각 조회
     */
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    /**
     * 생성 시각 조회
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시각 조회
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 파일 생성 팩토리 메서드
     * <p>
     * UUID v7을 자동 생성하고 초기 상태를 PENDING으로 설정합니다.
     * </p>
     *
     * @param fileName    파일명
     * @param fileSize    파일 크기 (바이트)
     * @param mimeType    MIME 타입
     * @param s3Key       S3 객체 키
     * @param s3Bucket    S3 버킷명
     * @param uploaderId  업로더 사용자 ID
     * @param category    파일 카테고리
     * @param tags        태그 (콤마 구분, nullable)
     * @return 생성된 File Aggregate
     * @throws InvalidFileSizeException 파일 크기가 유효하지 않을 때
     * @throws InvalidMimeTypeException MIME 타입이 허용되지 않을 때
     */
    public static File create(
            String fileName,
            long fileSize,
            String mimeType,
            String s3Key,
            String s3Bucket,
            Long uploaderId,
            String category,
            String tags
    ) {
        // 파일 크기 검증
        validateFileSize(fileSize);

        // MIME 타입 검증
        validateMimeType(mimeType);

        // UUID v7 자동 생성
        String fileId = UuidV7Generator.generate();

        // 현재 시각
        LocalDateTime now = LocalDateTime.now();

        // CDN URL 생성 (S3 키 기반)
        String cdnUrl = CDN_BASE_URL + s3Key;

        return new File(
                fileId,
                fileName,
                fileSize,
                mimeType,
                FileStatus.PENDING, // 초기 상태는 PENDING
                s3Key,
                s3Bucket,
                cdnUrl,
                uploaderId,
                category,
                tags,
                1, // 초기 버전 1
                null, // deletedAt은 null
                now, // createdAt
                now  // updatedAt
        );
    }

    /**
     * 파일 크기 검증
     *
     * @param fileSize 파일 크기 (바이트)
     * @throws InvalidFileSizeException 파일 크기가 0 이하이거나 1GB 초과일 때
     */
    private static void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new InvalidFileSizeException("파일 크기는 0보다 커야 합니다. 현재 크기: " + fileSize + " bytes");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new InvalidFileSizeException(
                    "파일 크기는 1GB를 초과할 수 없습니다. 현재 크기: " + fileSize + " bytes, 최대 크기: " + MAX_FILE_SIZE + " bytes"
            );
        }
    }

    /**
     * MIME 타입 검증
     *
     * @param mimeType MIME 타입
     * @throws InvalidMimeTypeException MIME 타입이 허용 목록에 없을 때
     */
    private static void validateMimeType(String mimeType) {
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new InvalidMimeTypeException(
                    "허용되지 않는 MIME 타입입니다. 제공된 타입: " + mimeType
            );
        }
    }
}
