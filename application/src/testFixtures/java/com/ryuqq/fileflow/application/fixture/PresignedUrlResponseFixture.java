package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;

/**
 * PresignedUrlResponse TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aResponse(), create()
 * </p>
 */
public class PresignedUrlResponseFixture {

    /**
     * 기본 PresignedUrlResponse 생성
     */
    public static PresignedUrlResponse aResponse() {
        return new PresignedUrlResponse(
                1L,
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg?signature=abc123",
                3600L,
                "uploads/2024/11/16/file-1.jpg"
        );
    }

    /**
     * 기본 PresignedUrlResponse 생성 (alias)
     */
    public static PresignedUrlResponse create() {
        return aResponse();
    }

    /**
     * 커스텀 파일 ID로 Response 생성
     */
    public static PresignedUrlResponse withFileId(Long fileId) {
        return new PresignedUrlResponse(
                fileId,
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg?signature=abc123",
                3600L,
                "uploads/2024/11/16/file-1.jpg"
        );
    }

    /**
     * 커스텀 Presigned URL로 Response 생성
     */
    public static PresignedUrlResponse withPresignedUrl(String presignedUrl) {
        return new PresignedUrlResponse(
                1L,
                presignedUrl,
                3600L,
                "uploads/2024/11/16/file-1.jpg"
        );
    }

    /**
     * 커스텀 만료 시간으로 Response 생성
     */
    public static PresignedUrlResponse withExpiresIn(Long expiresIn) {
        return new PresignedUrlResponse(
                1L,
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg?signature=abc123",
                expiresIn,
                "uploads/2024/11/16/file-1.jpg"
        );
    }

    /**
     * 커스텀 S3 키로 Response 생성
     */
    public static PresignedUrlResponse withS3Key(String s3Key) {
        return new PresignedUrlResponse(
                1L,
                "https://s3.amazonaws.com/fileflow-bucket/" + s3Key + "?signature=abc123",
                3600L,
                s3Key
        );
    }

    /**
     * 단일 업로드용 Response (작은 파일, < 100MB)
     */
    public static PresignedUrlResponse singleUpload() {
        return new PresignedUrlResponse(
                2L,
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/small-file.jpg?signature=xyz789",
                3600L,
                "uploads/2024/11/16/small-file.jpg"
        );
    }

    /**
     * 멀티파트 업로드용 Response (큰 파일, >= 100MB)
     */
    public static PresignedUrlResponse multipartUpload() {
        return new PresignedUrlResponse(
                3L,
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/large-file.mp4?uploadId=ABC&signature=def456",
                7200L,
                "uploads/2024/11/16/large-file.mp4"
        );
    }

    /**
     * 짧은 만료 시간 Response (5분)
     */
    public static PresignedUrlResponse shortExpiry() {
        return new PresignedUrlResponse(
                4L,
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/temp-file.txt?signature=ghi789",
                300L,
                "uploads/2024/11/16/temp-file.txt"
        );
    }
}
