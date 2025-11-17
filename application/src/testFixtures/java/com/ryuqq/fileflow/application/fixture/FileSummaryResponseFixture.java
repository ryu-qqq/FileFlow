package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.response.FileSummaryResponse;

import java.time.LocalDateTime;

/**
 * FileSummaryResponse TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aResponse(), create()
 * </p>
 */
public class FileSummaryResponseFixture {

    /**
     * 기본 FileSummaryResponse 생성
     */
    public static FileSummaryResponse aResponse() {
        return new FileSummaryResponse(
                1L,
                "document.pdf",
                "COMPLETED",
                100L,
                LocalDateTime.of(2024, 11, 16, 10, 0, 0)
        );
    }

    /**
     * 기본 FileSummaryResponse 생성 (alias)
     */
    public static FileSummaryResponse create() {
        return aResponse();
    }

    /**
     * 커스텀 파일 ID로 Response 생성
     */
    public static FileSummaryResponse withFileId(Long fileId) {
        return new FileSummaryResponse(
                fileId,
                "document.pdf",
                "COMPLETED",
                100L,
                LocalDateTime.of(2024, 11, 16, 10, 0, 0)
        );
    }

    /**
     * 커스텀 파일명으로 Response 생성
     */
    public static FileSummaryResponse withFileName(String fileName) {
        return new FileSummaryResponse(
                1L,
                fileName,
                "COMPLETED",
                100L,
                LocalDateTime.of(2024, 11, 16, 10, 0, 0)
        );
    }

    /**
     * 커스텀 상태로 Response 생성
     */
    public static FileSummaryResponse withStatus(String status) {
        return new FileSummaryResponse(
                1L,
                "document.pdf",
                status,
                100L,
                LocalDateTime.of(2024, 11, 16, 10, 0, 0)
        );
    }

    /**
     * 커스텀 업로더 ID로 Response 생성
     */
    public static FileSummaryResponse withUploaderId(Long uploaderId) {
        return new FileSummaryResponse(
                1L,
                "document.pdf",
                "COMPLETED",
                uploaderId,
                LocalDateTime.of(2024, 11, 16, 10, 0, 0)
        );
    }

    /**
     * 커스텀 생성 시간으로 Response 생성
     */
    public static FileSummaryResponse withCreatedAt(LocalDateTime createdAt) {
        return new FileSummaryResponse(
                1L,
                "document.pdf",
                "COMPLETED",
                100L,
                createdAt
        );
    }

    /**
     * 업로드 대기 중 파일 요약
     */
    public static FileSummaryResponse pending() {
        return new FileSummaryResponse(
                2L,
                "pending-file.jpg",
                "PENDING",
                101L,
                LocalDateTime.of(2024, 11, 16, 10, 5, 0)
        );
    }

    /**
     * 업로드 진행 중 파일 요약
     */
    public static FileSummaryResponse uploading() {
        return new FileSummaryResponse(
                3L,
                "uploading-file.mp4",
                "UPLOADING",
                102L,
                LocalDateTime.of(2024, 11, 16, 10, 10, 0)
        );
    }

    /**
     * 업로드 완료된 파일 요약
     */
    public static FileSummaryResponse completed() {
        return new FileSummaryResponse(
                4L,
                "completed-file.pdf",
                "COMPLETED",
                103L,
                LocalDateTime.of(2024, 11, 16, 10, 15, 0)
        );
    }

    /**
     * 업로드 실패한 파일 요약
     */
    public static FileSummaryResponse failed() {
        return new FileSummaryResponse(
                5L,
                "failed-file.zip",
                "FAILED",
                104L,
                LocalDateTime.of(2024, 11, 16, 10, 20, 0)
        );
    }

    /**
     * 이미지 파일 요약
     */
    public static FileSummaryResponse imageFile() {
        return new FileSummaryResponse(
                6L,
                "profile-photo.jpg",
                "COMPLETED",
                105L,
                LocalDateTime.of(2024, 11, 16, 10, 25, 0)
        );
    }

    /**
     * 문서 파일 요약
     */
    public static FileSummaryResponse documentFile() {
        return new FileSummaryResponse(
                7L,
                "annual-report.pdf",
                "COMPLETED",
                106L,
                LocalDateTime.of(2024, 11, 16, 10, 30, 0)
        );
    }

    /**
     * 동영상 파일 요약
     */
    public static FileSummaryResponse videoFile() {
        return new FileSummaryResponse(
                8L,
                "tutorial-video.mp4",
                "COMPLETED",
                107L,
                LocalDateTime.of(2024, 11, 16, 10, 35, 0)
        );
    }

    /**
     * 최근 업로드 파일 요약 (오늘)
     */
    public static FileSummaryResponse recentUpload() {
        return new FileSummaryResponse(
                9L,
                "recent-file.docx",
                "COMPLETED",
                108L,
                LocalDateTime.now()
        );
    }

    /**
     * 오래된 파일 요약 (1년 전)
     */
    public static FileSummaryResponse oldFile() {
        return new FileSummaryResponse(
                10L,
                "old-archive.zip",
                "COMPLETED",
                109L,
                LocalDateTime.now().minusYears(1)
        );
    }
}
