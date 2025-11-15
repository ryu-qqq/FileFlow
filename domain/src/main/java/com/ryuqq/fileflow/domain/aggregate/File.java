package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.vo.FileStatus;

import java.time.LocalDateTime;

/**
 * 파일 Aggregate Root
 * <p>
 * 파일 메타데이터와 S3 저장 정보를 관리하는 도메인 엔티티입니다.
 * </p>
 */
public class File {

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
}
