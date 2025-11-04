package com.ryuqq.fileflow.adapter.rest.file.dto.response;

import java.time.LocalDateTime;

/**
 * File Metadata API Response
 *
 * <p>파일 메타데이터 조회 응답 DTO입니다.</p>
 *
 * <p><strong>응답 예시:</strong></p>
 * <pre>{@code
 * {
 *   "fileId": 123,
 *   "fileName": "document.pdf",
 *   "fileSize": 1024000,
 *   "contentType": "application/pdf",
 *   "status": "AVAILABLE",
 *   "visibility": "PRIVATE",
 *   "storageKey": "tenant-1/org-2/file-123.pdf",
 *   "uploadedAt": "2024-10-31T12:00:00",
 *   "expiresAt": "2024-11-30T12:00:00"
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileMetadataApiResponse {

    private Long fileId;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String status;
    private String visibility;
    private String storageKey;
    private LocalDateTime uploadedAt;
    private LocalDateTime expiresAt;


    /**
     * Private 생성자
     */
    private FileMetadataApiResponse() {
    }

    /**
     * Static Factory Method
     *
     * @param fileId File ID
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType Content Type
     * @param status 파일 상태
     * @param visibility 파일 가시성
     * @param storageKey 스토리지 키
     * @param uploadedAt 업로드 시간
     * @param expiresAt 만료 시간
     * @return FileMetadataApiResponse
     */
    public static FileMetadataApiResponse of(
        Long fileId,
        String fileName,
        Long fileSize,
        String contentType,
        String status,
        String visibility,
        String storageKey,
        LocalDateTime uploadedAt,
        LocalDateTime expiresAt
    ) {
        FileMetadataApiResponse response = new FileMetadataApiResponse();
        response.fileId = fileId;
        response.fileName = fileName;
        response.fileSize = fileSize;
        response.contentType = contentType;
        response.status = status;
        response.visibility = visibility;
        response.storageKey = storageKey;
        response.uploadedAt = uploadedAt;
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
     * File Size Getter
     *
     * @return 파일 크기 (bytes)
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Content Type Getter
     *
     * @return Content Type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Status Getter
     *
     * @return 파일 상태
     */
    public String getStatus() {
        return status;
    }

    /**
     * Visibility Getter
     *
     * @return 파일 가시성
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * Storage Key Getter
     *
     * @return 스토리지 키
     */
    public String getStorageKey() {
        return storageKey;
    }

    /**
     * Uploaded At Getter
     *
     * @return 업로드 시간
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    /**
     * Expires At Getter
     *
     * @return 만료 시간
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
