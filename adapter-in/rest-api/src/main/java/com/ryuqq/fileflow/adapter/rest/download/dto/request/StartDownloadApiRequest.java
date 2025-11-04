package com.ryuqq.fileflow.adapter.rest.download.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * External Download 시작 API Request
 *
 * <p>외부 URL로부터 파일 다운로드를 시작하는 요청 DTO입니다.</p>
 *
 * <p><strong>Request Body 예시:</strong></p>
 * <pre>{@code
 * {
 *   "sourceUrl": "https://example.com/files/document.pdf",
 *   "fileName": "document.pdf"
 * }
 * }</pre>
 *
 * @param sourceUrl 소스 URL
 * @param fileName 저장할 파일명 (선택)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record StartDownloadApiRequest(
    @NotBlank(message = "Source URL is required")
    @Pattern(regexp = "^https?://.*", message = "Must be HTTP or HTTPS URL")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    String sourceUrl,

    @Size(max = 255, message = "File name must not exceed 255 characters")
    String fileName
) {
    /**
     * Static Factory Method
     *
     * @param sourceUrl 소스 URL
     * @return StartDownloadApiRequest 인스턴스
     */
    public static StartDownloadApiRequest of(String sourceUrl) {
        return new StartDownloadApiRequest(sourceUrl, null);
    }

    /**
     * Static Factory Method with fileName
     *
     * @param sourceUrl 소스 URL
     * @param fileName 저장할 파일명
     * @return StartDownloadApiRequest 인스턴스
     */
    public static StartDownloadApiRequest of(String sourceUrl, String fileName) {
        return new StartDownloadApiRequest(sourceUrl, fileName);
    }
}
