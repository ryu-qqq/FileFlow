package com.ryuqq.fileflow.application.session.port.out.command;

import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import java.time.Duration;

/**
 * 업로드 세션 캐시 Persist Port.
 *
 * <p>Redis를 통한 세션 캐싱을 담당합니다.
 *
 * <p><strong>캐시 전략</strong>:
 *
 * <ul>
 *   <li>TTL 기반 자동 만료 (단일: 15분, 멀티파트: 24시간)
 *   <li>Best-Effort 저장 (실패 시 RDB 폴백 가능)
 * </ul>
 */
public interface PersistUploadSessionCachePort {

    /**
     * 단일 업로드 세션을 캐시에 저장합니다.
     *
     * <p>TTL이 설정되며, 만료 시 자동 삭제됩니다.
     *
     * @param session 저장할 세션
     * @param ttl Time-To-Live (만료 시간)
     */
    void persist(SingleUploadSession session, Duration ttl);

    /**
     * 멀티파트 업로드 세션을 캐시에 저장합니다.
     *
     * <p>TTL이 설정되며, 만료 시 자동 삭제됩니다.
     *
     * @param session 저장할 세션
     * @param ttl Time-To-Live (만료 시간)
     */
    void persist(MultipartUploadSession session, Duration ttl);
}
