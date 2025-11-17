package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.response.FileDetailResponse;
import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;

import java.util.ArrayList;
import java.util.List;

/**
 * FileDetailResponse TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aResponse(), create()
 * </p>
 */
public class FileDetailResponseFixture {

    /**
     * 기본 FileDetailResponse 생성 (처리 작업 빈 리스트)
     */
    public static FileDetailResponse aResponse() {
        return new FileDetailResponse(
                1L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-1.jpg",
                new ArrayList<>()
        );
    }

    /**
     * 기본 FileDetailResponse 생성 (alias)
     */
    public static FileDetailResponse create() {
        return aResponse();
    }

    /**
     * 커스텀 파일 ID로 Response 생성
     */
    public static FileDetailResponse withFileId(Long fileId) {
        return new FileDetailResponse(
                fileId,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-1.jpg",
                new ArrayList<>()
        );
    }

    /**
     * 커스텀 상태로 Response 생성
     */
    public static FileDetailResponse withStatus(String status) {
        return new FileDetailResponse(
                1L,
                status,
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-1.jpg",
                new ArrayList<>()
        );
    }

    /**
     * 커스텀 S3 URL로 Response 생성
     */
    public static FileDetailResponse withS3Url(String s3Url) {
        return new FileDetailResponse(
                1L,
                "COMPLETED",
                s3Url,
                "https://cdn.example.com/uploads/2024/11/16/file-1.jpg",
                new ArrayList<>()
        );
    }

    /**
     * 커스텀 CDN URL로 Response 생성
     */
    public static FileDetailResponse withCdnUrl(String cdnUrl) {
        return new FileDetailResponse(
                1L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-1.jpg",
                cdnUrl,
                new ArrayList<>()
        );
    }

    /**
     * 처리 작업이 포함된 Response 생성 (빈 리스트 생성 후 add 가능하도록)
     */
    public static FileDetailResponse withProcessingJobs() {
        List<FileProcessingJob> jobs = new ArrayList<>();
        // Note: 실제 FileProcessingJob 추가는 도메인 Fixture가 필요합니다
        return new FileDetailResponse(
                2L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-2.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-2.jpg",
                jobs
        );
    }

    /**
     * 처리 작업이 없는 Response 생성
     */
    public static FileDetailResponse withoutProcessingJobs() {
        return new FileDetailResponse(
                3L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-3.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-3.jpg",
                new ArrayList<>()
        );
    }

    /**
     * 여러 처리 작업이 포함된 Response 생성 (복수 작업용)
     */
    public static FileDetailResponse withMultipleJobs() {
        List<FileProcessingJob> jobs = new ArrayList<>();
        // Note: 실제 사용 시 여러 FileProcessingJob 추가 가능
        return new FileDetailResponse(
                4L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-4.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-4.jpg",
                jobs
        );
    }

    /**
     * 업로드 대기 중 Response (처리 작업 없음)
     */
    public static FileDetailResponse pending() {
        return new FileDetailResponse(
                5L,
                "PENDING",
                null,
                null,
                new ArrayList<>()
        );
    }

    /**
     * 업로드 완료 + 썸네일 생성 중 Response
     */
    public static FileDetailResponse withThumbnailProcessing() {
        return new FileDetailResponse(
                6L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/uploads/2024/11/16/file-6.jpg",
                "https://cdn.example.com/uploads/2024/11/16/file-6.jpg",
                new ArrayList<>()
        );
    }

    /**
     * 프로필 이미지 + 메타데이터 추출 완료 Response
     */
    public static FileDetailResponse profileWithMetadata() {
        return new FileDetailResponse(
                7L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/profiles/user-123.jpg",
                "https://cdn.example.com/profiles/user-123.jpg",
                new ArrayList<>()
        );
    }

    /**
     * 문서 파일 + 압축 완료 Response
     */
    public static FileDetailResponse documentWithCompress() {
        return new FileDetailResponse(
                8L,
                "COMPLETED",
                "https://s3.amazonaws.com/fileflow-bucket/documents/report.pdf",
                "https://cdn.example.com/documents/report.pdf",
                new ArrayList<>()
        );
    }
}
