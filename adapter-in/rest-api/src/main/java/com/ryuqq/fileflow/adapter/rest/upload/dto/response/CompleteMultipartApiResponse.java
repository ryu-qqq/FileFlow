package com.ryuqq.fileflow.adapter.rest.upload.dto.response;

/**
 * Multipart Upload 완료 API Response
 *
 * <p>Multipart Upload 완료 성공 시 반환되는 응답 DTO입니다.</p>
 *
 * <p><strong>Response Body 예시:</strong></p>
 * <pre>{@code
 * {
 *   "fileId": 12345,
 *   "etag": "\"d41d8cd98f00b204e9800998ecf8427e\"",
 *   "location": "https://s3.amazonaws.com/bucket/uploads/2024/10/31/large-video.mp4"
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class CompleteMultipartApiResponse {

    private Long fileId;
    private String etag;
    private String location;

    /**
     * Private 생성자
     */
    private CompleteMultipartApiResponse() {
    }

    /**
     * 전체 생성자
     *
     * @param fileId 파일 ID
     * @param etag S3 ETag
     * @param location S3 Location URL
     */
    private CompleteMultipartApiResponse(Long fileId, String etag, String location) {
        this.fileId = fileId;
        this.etag = etag;
        this.location = location;
    }

    /**
     * Static Factory Method
     *
     * @param fileId 파일 ID
     * @param etag S3 ETag
     * @param location S3 Location URL
     * @return CompleteMultipartApiResponse 인스턴스
     */
    public static CompleteMultipartApiResponse of(Long fileId, String etag, String location) {
        return new CompleteMultipartApiResponse(fileId, etag, location);
    }

    /**
     * 파일 ID를 반환합니다.
     *
     * @return 파일 ID
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * S3 ETag를 반환합니다.
     *
     * @return S3 ETag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * S3 Location URL을 반환합니다.
     *
     * @return S3 Location URL
     */
    public String getLocation() {
        return location;
    }
}
