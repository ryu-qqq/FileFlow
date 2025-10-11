package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.UploadSession;

/**
 * 업로드 세션 캐시 Port
 *
 * Redis와 같은 캐시 저장소에 세션을 저장하고 관리하는 Outbound Port입니다.
 * TTL 기반 만료 감지를 위해 세션 정보를 캐시에 저장합니다.
 *
 * 설계 원칙:
 * - Best Effort: 캐시 저장 실패해도 예외를 전파하지 않음
 * - 캐시는 보조 저장소로, 실패해도 시스템은 계속 동작
 *
 * @author sangwon-ryu
 */
public interface UploadSessionCachePort {

    /**
     * Redis에 세션을 TTL과 함께 저장합니다.
     *
     * Best Effort 방식으로 동작하며, 저장 실패 시 로그만 남기고 예외를 전파하지 않습니다.
     *
     * @param session 저장할 UploadSession
     */
    void saveWithTtl(UploadSession session);

    /**
     * Redis에서 세션을 삭제합니다.
     *
     * Best Effort 방식으로 동작하며, 삭제 실패 시 로그만 남기고 예외를 전파하지 않습니다.
     *
     * @param sessionId 세션 ID
     */
    void delete(String sessionId);
}
