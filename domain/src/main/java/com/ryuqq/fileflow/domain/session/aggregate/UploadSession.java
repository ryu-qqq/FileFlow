package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.Instant;

/**
 * 업로드 세션 공통 인터페이스.
 *
 * <p>Single/Multipart 업로드 세션의 공통 동작을 정의합니다.
 */
public interface UploadSession {

    /**
     * 세션 ID를 반환한다.
     *
     * @return 세션 ID
     */
    UploadSessionId getId();

    /**
     * 세션 상태를 반환한다.
     *
     * @return 세션 상태
     */
    SessionStatus getStatus();

    /**
     * S3 버킷을 반환한다.
     *
     * @return S3 버킷
     */
    S3Bucket getBucket();

    /**
     * S3 객체 키를 반환한다.
     *
     * @return S3 객체 키
     */
    S3Key getS3Key();

    /**
     * 만료 시각을 반환한다.
     *
     * @return 만료 시각 (UTC 기준 Instant)
     */
    Instant getExpiresAt();

    /**
     * 세션을 만료 처리한다.
     *
     * @param clock 시간 소스
     * @throws com.ryuqq.fileflow.domain.session.exception.InvalidSessionStatusException 상태 전환 불가능한
     *     경우
     */
    void expire(Clock clock);
}
