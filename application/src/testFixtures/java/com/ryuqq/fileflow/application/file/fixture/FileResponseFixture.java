package com.ryuqq.fileflow.application.file.fixture;

import com.ryuqq.fileflow.application.dto.response.FileResponse;

/**
 * FileResponse TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aResponse(), create()
 * </p>
 */
public class FileResponseFixture {

    /**
     * 기본 FileResponse 생성
     */
    public static FileResponse aResponse() {
        return new FileResponse(
                1L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-1.jpg"
        );
    }

    /**
     * 기본 FileResponse 생성 (alias)
     */
    public static FileResponse create() {
        return aResponse();
    }

    /**
     * 커스텀 파일 ID로 Response 생성
     */
    public static FileResponse withFileId(Long fileId) {
        return new FileResponse(
                fileId,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-1.jpg"
        );
    }

    /**
     * 커스텀 상태로 Response 생성
     */
    public static FileResponse withStatus(String status) {
        return new FileResponse(
                1L,
                status,
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-1.jpg"
        );
    }

    /**
     * 커스텀 S3 URL로 Response 생성
     */
    public static FileResponse withS3Url(String s3Url) {
        return new FileResponse(
                1L,
                "COMPLETED",
                s3Url,
                "https://cdn.example.com/uploads/2024/11/16/file-1.jpg"
        );
    }

    /**
     * 커스텀 CDN URL로 Response 생성
     */
    public static FileResponse withCdnUrl(String cdnUrl) {
        return new FileResponse(
                1L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg",
                cdnUrl
        );
    }

    /**
     * 업로드 대기 중 Response
     */
    public static FileResponse pending() {
        return new FileResponse(
                2L,
                "PENDING",
                null,
                null
        );
    }

    /**
     * 업로드 진행 중 Response
     */
    public static FileResponse uploading() {
        return new FileResponse(
                3L,
                "UPLOADING",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-3.jpg",
                null
        );
    }

    /**
     * 업로드 완료 Response (S3 + CDN URL 모두 포함)
     */
    public static FileResponse completed() {
        return new FileResponse(
                4L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-4.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-4.jpg"
        );
    }

    /**
     * 업로드 실패 Response
     */
    public static FileResponse failed() {
        return new FileResponse(
                5L,
                "FAILED",
                null,
                null
        );
    }

    /**
     * 프로필 이미지 Response
     */
    public static FileResponse profileImage() {
        return new FileResponse(
                6L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/profiles/user-123.jpg",
                "https://cdn.example.com/profiles/user-123.jpg"
        );
    }

    /**
     * 문서 파일 Response
     */
    public static FileResponse document() {
        return new FileResponse(
                7L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/documents/report.pdf",
                "https://cdn.example.com/documents/report.pdf"
        );
    }
}
