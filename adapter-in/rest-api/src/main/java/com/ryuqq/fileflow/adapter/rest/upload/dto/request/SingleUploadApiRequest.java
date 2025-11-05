package com.ryuqq.fileflow.adapter.rest.upload.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Single Upload API Request
 *
 * <p>100MB 미만 파일의 단일 업로드를 위한 요청 DTO입니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>파일 크기가 100MB 미만인 경우</li>
 *   <li>단일 HTTP PUT으로 업로드 가능한 파일</li>
 *   <li>Multipart Upload보다 효율적인 방식</li>
 * </ul>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>fileName: 필수, 최대 255자</li>
 *   <li>fileSize: 필수, 양수, 최대 100MB (104,857,600 bytes)</li>
 *   <li>contentType: 필수, 최대 100자</li>
 *   <li>checksum: 선택, 최대 64자</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class SingleUploadApiRequest {

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be positive")
    @Max(value = 104_857_600, message = "File size must be less than 100MB for single upload")
    private Long fileSize;

    @NotBlank(message = "Content type is required")
    @Size(max = 100, message = "Content type must not exceed 100 characters")
    private String contentType;

    @Size(max = 64, message = "Checksum must not exceed 64 characters")
    private String checksum;

    /**
     * Default Constructor
     */
    public SingleUploadApiRequest() {
    }

    /**
     * Full Constructor
     *
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType 콘텐츠 타입
     * @param checksum 체크섬 (선택)
     */
    public SingleUploadApiRequest(
        String fileName,
        Long fileSize,
        String contentType,
        String checksum
    ) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.checksum = checksum;
    }

    /**
     * 파일명 조회
     *
     * @return 파일명
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 파일명 설정
     *
     * @param fileName 파일명
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 파일 크기 조회
     *
     * @return 파일 크기 (bytes)
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 파일 크기 설정
     *
     * @param fileSize 파일 크기 (bytes)
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 콘텐츠 타입 조회
     *
     * @return 콘텐츠 타입
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 콘텐츠 타입 설정
     *
     * @param contentType 콘텐츠 타입
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 체크섬 조회
     *
     * @return 체크섬 (선택)
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * 체크섬 설정
     *
     * @param checksum 체크섬
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
