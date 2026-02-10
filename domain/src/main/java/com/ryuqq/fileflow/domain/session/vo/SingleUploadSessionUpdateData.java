package com.ryuqq.fileflow.domain.session.vo;

/**
 * 단건 업로드 세션 완료 시 필요한 업데이트 데이터.
 *
 * <p>S3 HeadObject로 검증된 파일 크기와 ETag를 담습니다.
 *
 * @param fileSize 파일 크기 (bytes)
 * @param etag S3 ETag
 */
public record SingleUploadSessionUpdateData(long fileSize, String etag) {

    public static SingleUploadSessionUpdateData of(long fileSize, String etag) {
        return new SingleUploadSessionUpdateData(fileSize, etag);
    }
}
