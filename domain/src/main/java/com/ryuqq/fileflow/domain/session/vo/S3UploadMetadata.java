package com.ryuqq.fileflow.domain.session.vo;

/**
 * S3 업로드 메타데이터 VO.
 *
 * <p>S3 Multipart Upload 초기화에 필요한 최소 정보만 담는 경량 VO입니다.
 *
 * <p><strong>사용 목적</strong>:
 *
 * <ul>
 *   <li>불필요한 전체 세션 생성 방지 (temp 세션 생성 불필요)
 *   <li>S3 Upload ID 발급에 필요한 최소 정보만 제공
 *   <li>Command → S3 API 호출 간 중간 데이터 전달
 * </ul>
 *
 * <p><strong>포함 정보</strong>:
 *
 * <ul>
 *   <li>S3 Bucket: 업로드 대상 버킷
 *   <li>S3 Key: 업로드 객체 키 (경로 포함)
 *   <li>Content-Type: 파일 MIME 타입
 * </ul>
 */
public record S3UploadMetadata(S3Bucket bucket, S3Key s3Key, ContentType contentType) {

    /**
     * S3 업로드 메타데이터 생성.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param contentType Content-Type
     * @return S3UploadMetadata
     */
    public static S3UploadMetadata of(S3Bucket bucket, S3Key s3Key, ContentType contentType) {
        return new S3UploadMetadata(bucket, s3Key, contentType);
    }

    /**
     * S3 버킷 이름 반환.
     *
     * @return 버킷 이름 (String)
     */
    public S3Bucket getBucket() {
        return bucket;
    }

    /**
     * S3 객체 키 반환.
     *
     * @return 객체 키 (String)
     */
    public S3Key getS3Key() {
        return s3Key;
    }

    /**
     * Content-Type 반환.
     *
     * @return Content-Type (String)
     */
    public ContentType getContentType() {
        return contentType;
    }
}
