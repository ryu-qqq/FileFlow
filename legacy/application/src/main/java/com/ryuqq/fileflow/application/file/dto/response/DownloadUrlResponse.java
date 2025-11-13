package com.ryuqq.fileflow.application.file.dto.response;

import java.time.LocalDateTime;

/**
 * DownloadUrl Response DTO
 *
 * <p>CQRS Command Side - 다운로드 URL 생성 응답</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>서명된 다운로드 URL 반환</li>
 *   <li>URL 만료 시간 정보 제공</li>
 *   <li>파일 메타데이터 간략 정보</li>
 * </ul>
 *
 * <p><strong>사용 시나리오</strong>:</p>
 * <ul>
 *   <li>프론트엔드로 다운로드 링크 전달</li>
 *   <li>임시 공유 링크 생성</li>
 *   <li>API 응답에 포함</li>
 * </ul>
 *
 * <p><strong>보안</strong>:</p>
 * <ul>
 *   <li>서명된 URL (S3 Presigned URL)</li>
 *   <li>만료 시간 포함 (기본 1시간)</li>
 *   <li>일회성 사용 권장 (재사용 시 보안 위험)</li>
 * </ul>
 *
 * @param fileId 파일 ID
 * @param fileName 파일명
 * @param downloadUrl 서명된 다운로드 URL
 * @param expiresAt URL 만료 시간
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record DownloadUrlResponse(
    Long fileId,
    String fileName,
    String downloadUrl,
    LocalDateTime expiresAt
) {
    /**
     * Compact Constructor (검증 로직)
     */
    public DownloadUrlResponse {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId는 필수입니다");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("FileName은 필수입니다");
        }
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new IllegalArgumentException("DownloadUrl은 필수입니다");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("ExpiresAt은 필수입니다");
        }
    }

    /**
     * Static Factory Method
     *
     * @param fileId 파일 ID
     * @param fileName 파일명
     * @param downloadUrl 서명된 다운로드 URL
     * @param expiresAt URL 만료 시간
     * @return DownloadUrlResponse
     */
    public static DownloadUrlResponse of(
        Long fileId,
        String fileName,
        String downloadUrl,
        LocalDateTime expiresAt
    ) {
        return new DownloadUrlResponse(fileId, fileName, downloadUrl, expiresAt);
    }
}
