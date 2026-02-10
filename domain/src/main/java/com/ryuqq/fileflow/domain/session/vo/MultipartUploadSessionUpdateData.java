package com.ryuqq.fileflow.domain.session.vo;

/**
 * 멀티파트 업로드 세션 완료 시 필요한 업데이트 데이터.
 *
 * <p>S3 CompleteMultipartUpload로 검증된 전체 파일 크기와 ETag를 담습니다.
 *
 * @param totalFileSize 전체 파일 크기 (bytes)
 * @param etag S3 ETag
 */
public record MultipartUploadSessionUpdateData(long totalFileSize, String etag) {

    public static MultipartUploadSessionUpdateData of(long totalFileSize, String etag) {
        return new MultipartUploadSessionUpdateData(totalFileSize, etag);
    }
}
