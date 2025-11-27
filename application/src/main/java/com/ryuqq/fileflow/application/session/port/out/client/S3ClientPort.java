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
 * S3 연동 Port.
 *
 * <p>S3 Presigned URL 발급 및 Multipart Upload 관리를 담당합니다.
 */
public interface S3ClientPort {

    /**
     * 단일 업로드용 Presigned URL을 발급합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param contentType Content-Type
     * @param duration 유효 기간 (15분)
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
     * @param duration 유효 기간 (24시간)
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
     * <p>모든 Part의 ETag와 Part 번호를 S3에 전달하여 최종 병합을 수행합니다.
     *
     * <p><strong>S3 검증</strong>:
     *
     * <ul>
     *   <li>S3가 모든 Part의 ETag를 검증합니다.
     *   <li>ETag가 하나라도 틀리면 S3가 예외를 발생시킵니다.
     *   <li>Part 번호 순서대로 전달해야 합니다 (Domain에서 정렬 보장).
     * </ul>
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
     * <p>세션 만료 또는 취소 시 S3에 업로드된 Part들을 정리합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param uploadId S3 Upload ID
     */
    void abortMultipartUpload(S3Bucket bucket, S3Key s3Key, String uploadId);

    /**
     * 바이트 배열을 S3에 직접 업로드합니다.
     *
     * <p>외부 다운로드 Worker에서 다운로드한 이미지를 S3에 업로드할 때 사용합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param contentType Content-Type
     * @param data 업로드할 바이트 배열
     * @return 업로드된 객체의 ETag
     */
    ETag putObject(S3Bucket bucket, S3Key s3Key, ContentType contentType, byte[] data);

    /**
     * S3 객체의 다운로드용 Presigned URL을 발급합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param duration 유효 기간
     * @return Presigned GET URL
     */
    String generatePresignedGetUrl(S3Bucket bucket, S3Key s3Key, Duration duration);
}
