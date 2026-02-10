package com.ryuqq.fileflow.application.session.port.out.client;

import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import java.time.Duration;
import java.util.List;

/**
 * 멀티파트 업로드 클라이언트
 *
 * <p>멀티파트 업로드 생성, 파트 URL 발급, 완료, 중단을 추상화합니다.
 */
public interface MultipartUploadClient {

    /**
     * 멀티파트 업로드 시작
     *
     * @param s3Key 객체 키
     * @param contentType MIME 타입
     * @return uploadId
     */
    String createMultipartUpload(String s3Key, String contentType);

    /**
     * 파트 업로드용 Presigned URL 생성
     *
     * @param s3Key 객체 키
     * @param uploadId 업로드 ID
     * @param partNumber 파트 번호
     * @param ttl URL 유효 기간
     * @return Presigned URL 문자열
     */
    String generatePresignedPartUrl(String s3Key, String uploadId, int partNumber, Duration ttl);

    /**
     * 멀티파트 업로드 완료
     *
     * @param s3Key 객체 키
     * @param uploadId 업로드 ID
     * @param parts 완료된 파트 목록
     * @return 완료된 객체의 ETag
     */
    String completeMultipartUpload(String s3Key, String uploadId, List<CompletedPart> parts);

    /**
     * 멀티파트 업로드 중단
     *
     * @param s3Key 객체 키
     * @param uploadId 업로드 ID
     */
    void abortMultipartUpload(String s3Key, String uploadId);
}
