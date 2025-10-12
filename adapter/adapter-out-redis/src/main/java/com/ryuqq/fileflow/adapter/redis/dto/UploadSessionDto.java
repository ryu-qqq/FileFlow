package com.ryuqq.fileflow.adapter.redis.dto;

import com.ryuqq.fileflow.domain.upload.UploadSession;

import java.time.LocalDateTime;

/**
 * Redis에 저장될 UploadSession DTO (Java Record)
 *
 * TTL 기반 만료 감지를 위한 최소 정보만 포함합니다.
 * 실제 영구 데이터는 DB에 저장되며, Redis는 만료 이벤트 발생용으로만 사용됩니다.
 *
 * Java record 사용으로 다음과 같은 이점을 제공합니다:
 * - 불변성(Immutability) 자동 보장
 * - equals(), hashCode(), toString() 자동 생성
 * - Boilerplate 코드 제거 (104줄 → 27줄)
 * - Jackson 직렬화/역직렬화 자동 지원 (Java 17+)
 *
 * @param sessionId 세션 ID
 * @param uploaderId 업로더 ID
 * @param status 세션 상태
 * @param createdAt 생성 시간
 * @param expiresAt 만료 시간
 * @author sangwon-ryu
 */
public record UploadSessionDto(
        String sessionId,
        String uploaderId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    /**
     * Factory Method - Domain UploadSession에서 DTO 생성
     *
     * @param session 도메인 UploadSession 객체
     * @return UploadSessionDto
     */
    public static UploadSessionDto from(UploadSession session) {
        return new UploadSessionDto(
                session.getSessionId(),
                session.getUploaderId(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );
    }
}
