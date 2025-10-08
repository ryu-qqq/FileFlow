package com.ryuqq.fileflow.application.upload.port.out;

/**
 * S3 객체 검증 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * S3에 업로드된 파일의 존재 여부와 메타데이터를 검증하기 위한 인터페이스입니다.
 *
 * @author sangwon-ryu
 */
public interface VerifyS3ObjectPort {

    /**
     * S3에 객체가 존재하는지 확인합니다.
     *
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return 존재 여부
     */
    boolean doesObjectExist(String bucket, String key);

    /**
     * S3 객체의 ETag를 조회합니다.
     *
     * ETag는 S3에서 생성하는 객체의 해시값으로,
     * 파일 무결성 검증에 사용됩니다.
     *
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return ETag 값 (없으면 null)
     */
    String getObjectETag(String bucket, String key);

    /**
     * S3 객체의 메타데이터를 조회합니다.
     *
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return S3 객체 메타데이터
     */
    S3ObjectMetadata getObjectMetadata(String bucket, String key);

    /**
     * S3 객체 메타데이터를 담는 DTO
     *
     * @param etag S3 ETag
     * @param contentLength 파일 크기 (바이트)
     * @param contentType Content-Type
     * @param lastModified 마지막 수정 시간 (ISO-8601 형식)
     */
    record S3ObjectMetadata(
            String etag,
            long contentLength,
            String contentType,
            String lastModified
    ) {
    }
}
