package com.ryuqq.fileflow.application.session.dto.response;

import com.ryuqq.fileflow.domain.file.vo.S3Bucket;
import com.ryuqq.fileflow.domain.file.vo.S3Key;
import com.ryuqq.fileflow.domain.iam.vo.FileId;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;

/**
 * 세션 준비 결과 DTO
 * <p>
 * 세션 준비 과정의 결과를 담는 DTO입니다.
 * 멱등성 보장을 위해 새 세션과 기존 세션을 구분합니다.
 * </p>
 *
 * <p>
 * Factory Methods:
 * - {@link #newSession}: 새로 생성된 세션 (S3Key, S3Bucket 포함)
 * - {@link #existingSession}: 기존 세션 재사용 (S3Key, S3Bucket null)
 * </p>
 *
 * @param session 업로드 세션
 * @param fileId 파일 ID
 * @param s3Key S3 객체 키 (새 세션인 경우)
 * @param s3Bucket S3 버킷 (새 세션인 경우)
 * @param isExistingSession 기존 세션 여부
 */
public record SessionPreparationResult(
    UploadSession session,
    FileId fileId,
    S3Key s3Key,
    S3Bucket s3Bucket,
    boolean isExistingSession
) {
    /**
     * 기존 세션 결과 생성
     * <p>
     * 멱등성을 위해 동일한 sessionId로 재요청한 경우 사용합니다.
     * S3Key와 S3Bucket은 이미 생성되었으므로 null로 설정됩니다.
     * </p>
     *
     * @param session 기존 업로드 세션
     * @param fileId 기존 파일 ID
     * @param s3Key null (기존 세션이므로)
     * @param s3Bucket null (기존 세션이므로)
     * @return 기존 세션 결과
     */
    public static SessionPreparationResult existingSession(
        UploadSession session,
        FileId fileId,
        S3Key s3Key,
        S3Bucket s3Bucket
    ) {
        return new SessionPreparationResult(
            session,
            fileId,
            s3Key,
            s3Bucket,
            true
        );
    }

    /**
     * 새 세션 결과 생성
     * <p>
     * 최초 요청으로 새 세션을 생성한 경우 사용합니다.
     * S3Key와 S3Bucket이 함께 생성됩니다.
     * </p>
     *
     * @param session 새로 생성된 업로드 세션
     * @param fileId 새로 생성된 파일 ID
     * @param s3Key 생성된 S3 객체 키
     * @param s3Bucket 생성된 S3 버킷
     * @return 새 세션 결과
     */
    public static SessionPreparationResult newSession(
        UploadSession session,
        FileId fileId,
        S3Key s3Key,
        S3Bucket s3Bucket
    ) {
        return new SessionPreparationResult(
            session,
            fileId,
            s3Key,
            s3Bucket,
            false
        );
    }
}
