package com.ryuqq.fileflow.application.upload.port.out;

import java.time.Duration;

/**
 * Upload Session Cache Port (Outbound Port)
 *
 * <p>업로드 세션의 만료 추적을 위한 캐시 관리 Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>업로드 세션의 TTL 기반 만료 추적</li>
 *   <li>Presigned URL 만료 시점과 동기화된 캐시 관리</li>
 *   <li>만료 이벤트 발행을 위한 키 등록/해제</li>
 * </ul>
 *
 * <p><strong>구현체 요구사항:</strong></p>
 * <ul>
 *   <li>Redis Keyspace Notification 또는 유사한 TTL 기반 이벤트 메커니즘</li>
 *   <li>TTL 만료 시 자동 이벤트 발행 (UploadSessionExpirationListener)</li>
 *   <li>Session Key 기반 추적 가능</li>
 * </ul>
 *
 * <p><strong>아키텍처 패턴:</strong></p>
 * <ul>
 *   <li>Hexagonal Architecture: Application → Port ← Adapter</li>
 *   <li>Dependency Inversion: Application은 추상화에 의존, 구체 구현체와 분리</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface UploadSessionCachePort {

    /**
     * 업로드 세션을 캐시에 등록하여 TTL 기반 만료 추적 시작
     *
     * <p><strong>동작:</strong></p>
     * <ol>
     *   <li>Session Key를 Redis 키로 등록</li>
     *   <li>Presigned URL 만료 시간과 동일한 TTL 설정</li>
     *   <li>TTL 만료 시 Keyspace Notification 이벤트 자동 발행</li>
     * </ol>
     *
     * <p><strong>사용 시점:</strong></p>
     * <ul>
     *   <li>InitSingleUploadService: 단일 업로드 세션 생성 후</li>
     *   <li>InitMultipartUploadService: Multipart 업로드 세션 생성 후</li>
     * </ul>
     *
     * @param sessionKey 업로드 세션 키 (UploadSession.getSessionKey().value())
     * @param presignedUrlDuration Presigned URL 만료 시간 (TTL로 사용)
     */
    void trackSession(String sessionKey, Duration presignedUrlDuration);

    /**
     * 업로드 세션을 캐시에서 제거하여 만료 추적 중단
     *
     * <p><strong>사용 시점:</strong></p>
     * <ul>
     *   <li>업로드 성공 완료 시 (더 이상 만료 추적 불필요)</li>
     *   <li>업로드 명시적 취소 시</li>
     *   <li>수동 세션 정리 시</li>
     * </ul>
     *
     * @param sessionKey 업로드 세션 키
     */
    void untrackSession(String sessionKey);

    /**
     * 세션이 현재 추적 중인지 확인
     *
     * <p><strong>사용 시점:</strong></p>
     * <ul>
     *   <li>세션 상태 검증 시</li>
     *   <li>디버깅 및 모니터링</li>
     * </ul>
     *
     * @param sessionKey 업로드 세션 키
     * @return 추적 중이면 true, 아니면 false
     */
    boolean isTracking(String sessionKey);
}
