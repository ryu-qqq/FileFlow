package com.ryuqq.fileflow.application.session.port.out.client;

import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;

/**
 * 세션 만료 관리 클라이언트
 *
 * <p>Redis에 세션 만료 키를 등록/삭제합니다. TTL 만료 시 Redis keyspace notification을 통해 만료 이벤트가 발생합니다.
 */
public interface SessionExpirationClient {

    /**
     * 세션 만료 키 등록
     *
     * @param expiration 세션 만료 정보
     */
    void registerExpiration(SessionExpiration expiration);

    /**
     * 세션 만료 키 삭제 (세션 완료 시)
     *
     * @param sessionType 세션 유형 ("SINGLE" 또는 "MULTIPART")
     * @param sessionId 세션 ID
     */
    void removeExpiration(String sessionType, String sessionId);
}
