package com.ryuqq.fileflow.adapter.rest.upload.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Multipart Upload 초기화 API Request
 *
 * <p>대용량 파일의 분할 업로드를 시작하기 위한 요청 DTO입니다.</p>
 *
 * <p><strong>Request Body 예시:</strong></p>
 * <pre>{@code
 * {
 *   "fileName": "large-video.mp4",
 *   "fileSize": 524288000,
 *   "contentType": "video/mp4",
 *   "checksum": "d41d8cd98f00b204e9800998ecf8427e"
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InitMultipartApiRequest {

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be positive")
    private Long fileSize;

    @NotBlank(message = "Content type is required")
    @Size(max = 100, message = "Content type must not exceed 100 characters")
    private String contentType;

    @Size(max = 64, message = "Checksum must not exceed 64 characters")
    private String checksum;

    /**
     * Default 생성자
     */
    public InitMultipartApiRequest() {
    }

    /**
     * 전체 생성자
     *
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType MIME 타입
     * @param checksum 체크섬 (선택)
     */
    public InitMultipartApiRequest(String fileName, Long fileSize, String contentType, String checksum) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.checksum = checksum;
    }

    /**
     * 파일명을 반환합니다.
     *
     * @return 파일명
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 파일명을 설정합니다.
     *
     * @param fileName 파일명
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 파일 크기를 반환합니다.
     *
     * @return 파일 크기 (bytes)
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 파일 크기를 설정합니다.
     *
     * @param fileSize 파일 크기 (bytes)
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Content Type을 반환합니다.
     *
     * @return MIME 타입
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Content Type을 설정합니다.
     *
     * @param contentType MIME 타입
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 체크섬을 반환합니다.
     *
     * @return 체크섬 (선택)
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * 체크섬을 설정합니다.
     *
     * @param checksum 체크섬
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
