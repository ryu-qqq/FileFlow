package com.ryuqq.fileflow.domain.common.event;

import java.time.Instant;

/**
 * 도메인 이벤트 인터페이스
 *
 * <p>모든 도메인 이벤트는 이 인터페이스를 구현해야 합니다.
 *
 * <p><strong>구현 규칙</strong>:
 *
 * <ul>
 *   <li>Record 타입으로 구현 (불변성 보장)
 *   <li>occurredAt 필드 필수 (Instant 타입)
 *   <li>모든 필드는 Value Object 타입 사용
 *   <li>from(Aggregate, Instant) 정적 팩토리 메서드 제공
 * </ul>
 *
 * <p><strong>구현 예시</strong>:
 *
 * <pre>{@code
 * public record UploadCompletedEvent(
 *     String sessionId,
 *     String s3Key,
 *     long fileSize,
 *     Instant occurredAt
 * ) implements DomainEvent {
 *
 *     public static UploadCompletedEvent from(SingleUploadSession session, Instant occurredAt) {
 *         return new UploadCompletedEvent(
 *             session.idValue(),
 *             session.s3Key(),
 *             session.fileSize(),
 *             occurredAt
 *         );
 *     }
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface DomainEvent {

    /**
     * 이벤트 발생 시각
     *
     * <p>Aggregate의 Clock에서 획득한 시간을 사용합니다.
     *
     * @return 이벤트가 생성된 시각
     */
    Instant occurredAt();

    /**
     * 이벤트 타입 식별자
     *
     * <p>기본 구현은 클래스 단순명을 반환합니다.
     *
     * <p>이벤트 라우팅 및 로깅에 사용됩니다.
     *
     * @return 이벤트 타입 문자열
     */
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
