package com.ryuqq.fileflow.adapter.rest.download.dto.response;

/**
 * External Download 시작 API Response
 *
 * <p>외부 다운로드 시작 성공 시 반환되는 응답 DTO입니다.</p>
 * <p>⚠️ 비동기 처리: 다운로드는 백그라운드에서 진행되며, status API로 진행 상태를 확인할 수 있습니다.</p>
 *
 * <p><strong>Response Body 예시:</strong></p>
 * <pre>{@code
 * {
 *   "downloadId": 67890,
 *   "uploadSessionId": 12345,
 *   "status": "PENDING"
 * }
 * }</pre>
 *
 * @param downloadId 다운로드 ID
 * @param uploadSessionId 업로드 세션 ID
 * @param status 다운로드 상태
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record StartDownloadApiResponse(
    Long downloadId,
    Long uploadSessionId,
    String status
) {
    /**
     * Static Factory Method
     *
     * @param downloadId 다운로드 ID
     * @param uploadSessionId 업로드 세션 ID
     * @param status 다운로드 상태
     * @return StartDownloadApiResponse 인스턴스
     */
    public static StartDownloadApiResponse of(Long downloadId, Long uploadSessionId, String status) {
        return new StartDownloadApiResponse(downloadId, uploadSessionId, status);
    }
}
