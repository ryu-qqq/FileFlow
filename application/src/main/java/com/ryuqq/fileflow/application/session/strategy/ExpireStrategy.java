package com.ryuqq.fileflow.application.session.strategy;

import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;

/**
 * 업로드 세션 만료 전략 인터페이스.
 *
 * <p>세션 타입별로 만료 처리 방식이 다를 수 있으므로 전략 패턴을 사용합니다.
 *
 * <p><strong>구현체</strong>:
 *
 * <ul>
 *   <li>{@link SingleUploadExpireStrategy}: Single 업로드 만료 (Domain 만료만)
 *   <li>{@link MultipartUploadExpireStrategy}: Multipart 업로드 만료 (Domain 만료 + S3 Part 정리)
 * </ul>
 */
public interface ExpireStrategy<T extends UploadSession> {

    /**
     * 세션 만료 처리를 수행한다.
     *
     * @param session 만료 처리할 세션
     */
    void expire(T session);
}
