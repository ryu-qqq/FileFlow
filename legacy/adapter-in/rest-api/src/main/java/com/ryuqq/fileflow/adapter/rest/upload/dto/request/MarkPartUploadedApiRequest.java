package com.ryuqq.fileflow.adapter.rest.upload.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 파트 업로드 완료 통보 API Request
 *
 * <p>S3에 업로드 완료된 파트의 ETag와 크기를 전달하는 요청 DTO입니다.</p>
 *
 * <p><strong>Request Body 예시:</strong></p>
 * <pre>{@code
 * {
 *   "etag": "\"d41d8cd98f00b204e9800998ecf8427e\"",
 *   "partSize": 5242880
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MarkPartUploadedApiRequest {

    @NotBlank(message = "ETag is required")
    @Size(max = 255, message = "ETag must not exceed 255 characters")
    private String etag;

    @NotNull(message = "Part size is required")
    @Min(value = 1, message = "Part size must be positive")
    private Long partSize;

    /**
     * Default 생성자
     */
    public MarkPartUploadedApiRequest() {
    }

    /**
     * 전체 생성자
     *
     * @param etag S3 ETag
     * @param partSize 파트 크기 (bytes)
     */
    public MarkPartUploadedApiRequest(String etag, Long partSize) {
        this.etag = etag;
        this.partSize = partSize;
    }

    /**
     * ETag를 반환합니다.
     *
     * @return S3 ETag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * ETag를 설정합니다.
     *
     * @param etag S3 ETag
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * 파트 크기를 반환합니다.
     *
     * @return 파트 크기 (bytes)
     */
    public Long getPartSize() {
        return partSize;
    }

    /**
     * 파트 크기를 설정합니다.
     *
     * @param partSize 파트 크기 (bytes)
     */
    public void setPartSize(Long partSize) {
        this.partSize = partSize;
    }
}
