package com.ryuqq.fileflow.adapter.rest.download.dto.response;

/**
 * Download 상태 조회 API Response
 *
 * <p>다운로드 진행 상태 조회 응답 DTO입니다.</p>
 *
 * <p><strong>Response Body 예시:</strong></p>
 * <pre>{@code
 * {
 *   "downloadId": 67890,
 *   "status": "DOWNLOADING",
 *   "sourceUrl": "https://example.com/file.pdf",
 *   "uploadSessionId": 12345
 * }
 * }</pre>
 *
 * @param downloadId 다운로드 ID
 * @param status 다운로드 상태
 * @param sourceUrl 소스 URL
 * @param uploadSessionId 업로드 세션 ID
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record DownloadStatusApiResponse(
    Long downloadId,
    String status,
    String sourceUrl,
    Long uploadSessionId
) {
    /**
     * Static Factory Method
     *
     * @param downloadId 다운로드 ID
     * @param status 다운로드 상태
     * @param sourceUrl 소스 URL
     * @param uploadSessionId 업로드 세션 ID
     * @return DownloadStatusApiResponse 인스턴스
     */
    public static DownloadStatusApiResponse of(
        Long downloadId,
        String status,
        String sourceUrl,
        Long uploadSessionId
    ) {
        return new DownloadStatusApiResponse(downloadId, status, sourceUrl, uploadSessionId);
    }
}
