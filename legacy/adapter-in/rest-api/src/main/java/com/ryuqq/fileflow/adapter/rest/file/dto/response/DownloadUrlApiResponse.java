package com.ryuqq.fileflow.adapter.rest.file.dto.response;

import java.time.LocalDateTime;

/**
 * Download URL API Response
 *
 * <p>다운로드 URL 생성 응답 DTO입니다.</p>
 *
 * <p><strong>응답 예시:</strong></p>
 * <pre>{@code
 * {
 *   "fileId": 123,
 *   "fileName": "document.pdf",
 *   "downloadUrl": "https://bucket.s3.region.amazonaws.com/key?X-Amz-Signature=...",
 *   "expiresAt": "2024-10-31T13:00:00"
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class DownloadUrlApiResponse {

    private Long fileId;
    private String fileName;
    private String downloadUrl;
    private LocalDateTime expiresAt;

    /**
     * Private 생성자
     */
    private DownloadUrlApiResponse() {
    }

    /**
     * Static Factory Method
     *
     * @param fileId File ID
     * @param fileName 파일명
     * @param downloadUrl Presigned Download URL
     * @param expiresAt URL 만료 시간
     * @return DownloadUrlApiResponse
     */
    public static DownloadUrlApiResponse of(
        Long fileId,
        String fileName,
        String downloadUrl,
        LocalDateTime expiresAt
    ) {
        DownloadUrlApiResponse response = new DownloadUrlApiResponse();
        response.fileId = fileId;
        response.fileName = fileName;
        response.downloadUrl = downloadUrl;
        response.expiresAt = expiresAt;
        return response;
    }

    /**
     * File ID Getter
     *
     * @return File ID
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * File Name Getter
     *
     * @return 파일명
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Download URL Getter
     *
     * @return Presigned Download URL
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Expires At Getter
     *
     * @return URL 만료 시간
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
