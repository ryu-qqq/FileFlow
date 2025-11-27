package com.ryuqq.fileflow.application.session.port.out.command;

import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;

/**
 * 단일 업로드 세션 영속화 Command Port.
 *
 * <p>단일 업로드 세션을 RDB(upload_session_single 테이블)와 Redis Cache에 저장합니다.
 *
 * <p><strong>저장 전략</strong>:
 *
 * <ul>
 *   <li>RDB: upload_session_single 테이블 (폴백 및 영속성 보장)
 *   <li>Cache (Redis): session:single:{id} 키 (TTL 15분, 자동 만료 처리)
 * </ul>
 */
public interface SingleUploadSessionPersistencePort {

    /**
     * 단일 업로드 세션을 저장합니다.
     *
     * <p>RDB와 Redis Cache 모두에 저장하며, Cache에는 TTL(15분)이 설정됩니다.
     *
     * @param session 저장할 세션
     * @return 저장된 세션 (ID 포함)
     */
    SingleUploadSession persist(SingleUploadSession session);
}
