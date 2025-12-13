package com.ryuqq.fileflow.application.session.port.out.client;

import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 세션/업로드 전용 S3 클라이언트 포트.
 *
 * <p>Presigned URL 발급 및 Multipart Upload 관리를 담당합니다.
 *
 * <p><strong>사용처</strong>:
 *
 * <ul>
 *   <li>UploadSessionFacade - 업로드 세션 관리
 *   <li>CompleteSingleUploadService - 단일 업로드 완료
 *   <li>CompleteMultipartUploadService - 멀티파트 업로드 완료
 *   <li>MultipartUploadExpireStrategy - 업로드 세션 만료
 * </ul>
 */
public interface SessionS3ClientPort {

    /**
     * 단일 업로드용 Presigned URL을 발급합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param contentType Content-Type
     * @param duration 유효 기간
     * @return Presigned URL
     */
    String generatePresignedPutUrl(
            S3Bucket bucket, S3Key s3Key, ContentType contentType, Duration duration);

    /**
     * Multipart Upload를 초기화하고 Upload ID를 반환합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param contentType Content-Type
     * @return S3 Upload ID
     */
    String initiateMultipartUpload(S3Bucket bucket, S3Key s3Key, ContentType contentType);

    /**
     * Multipart Upload의 Part별 Presigned URL을 발급합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param uploadId S3 Upload ID
     * @param partNumber Part 번호 (1부터 시작)
     * @param duration 유효 기간
     * @return Presigned URL
     */
    String generatePresignedUploadPartUrl(
            S3Bucket bucket, S3Key s3Key, String uploadId, int partNumber, Duration duration);

    /**
     * S3 객체의 ETag를 조회합니다.
     *
     * <p>업로드 완료 시 클라이언트가 제공한 ETag와 S3에 실제 저장된 파일의 ETag를 비교하기 위해 사용합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @return S3 객체의 ETag (파일이 없으면 Optional.empty())
     */
    Optional<ETag> getObjectETag(S3Bucket bucket, S3Key s3Key);

    /**
     * Multipart Upload를 완료하고 S3에 병합을 요청합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param uploadId S3 Upload ID
     * @param completedParts 완료된 Part 목록 (Part 번호 순 정렬)
     * @return 병합된 파일의 ETag
     */
    ETag completeMultipartUpload(
            S3Bucket bucket, S3Key s3Key, String uploadId, List<CompletedPart> completedParts);

    /**
     * Multipart Upload를 중단하고 업로드된 Part들을 삭제합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param uploadId S3 Upload ID
     */
    void abortMultipartUpload(S3Bucket bucket, S3Key s3Key, String uploadId);
}
