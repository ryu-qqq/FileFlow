package com.ryuqq.fileflow.adapter.rest.upload.dto.response;

/**
 * Part Presigned URL API Response
 *
 * <p>파트 업로드를 위한 Presigned URL 생성 응답 DTO입니다.</p>
 *
 * <p><strong>Response Body 예시:</strong></p>
 * <pre>{@code
 * {
 *   "partNumber": 1,
 *   "presignedUrl": "https://s3.amazonaws.com/bucket/key?...",
 *   "expiresInSeconds": 3600
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PartPresignedUrlApiResponse {

    private Integer partNumber;
    private String presignedUrl;
    private Long expiresInSeconds;

    /**
     * Private 생성자
     */
    private PartPresignedUrlApiResponse() {
    }

    /**
     * 전체 생성자
     *
     * @param partNumber 파트 번호
     * @param presignedUrl Presigned URL
     * @param expiresInSeconds 만료 시간 (초)
     */
    private PartPresignedUrlApiResponse(Integer partNumber, String presignedUrl, Long expiresInSeconds) {
        this.partNumber = partNumber;
        this.presignedUrl = presignedUrl;
        this.expiresInSeconds = expiresInSeconds;
    }

    /**
     * Static Factory Method
     *
     * @param partNumber 파트 번호
     * @param presignedUrl Presigned URL
     * @param expiresInSeconds 만료 시간 (초)
     * @return PartPresignedUrlApiResponse 인스턴스
     */
    public static PartPresignedUrlApiResponse of(Integer partNumber, String presignedUrl, Long expiresInSeconds) {
        return new PartPresignedUrlApiResponse(partNumber, presignedUrl, expiresInSeconds);
    }

    /**
     * 파트 번호를 반환합니다.
     *
     * @return 파트 번호
     */
    public Integer getPartNumber() {
        return partNumber;
    }

    /**
     * Presigned URL을 반환합니다.
     *
     * @return Presigned URL
     */
    public String getPresignedUrl() {
        return presignedUrl;
    }

    /**
     * 만료 시간을 반환합니다.
     *
     * @return 만료 시간 (초)
     */
    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
