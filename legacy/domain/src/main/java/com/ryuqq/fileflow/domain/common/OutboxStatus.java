package com.ryuqq.fileflow.domain.common;

/**
 * Outbox Status Enum
 *
 * <p>Outbox 패턴의 메시지 처리 상태를 나타냅니다.</p>
 *
 * <p><strong>상태 전이:</strong></p>
 * <ul>
 *   <li>PENDING → PROCESSING → COMPLETED</li>
 *   <li>PENDING → PROCESSING → FAILED</li>
 *   <li>FAILED → PROCESSING (재시도)</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>PENDING: 메시지가 생성되었으나 아직 처리되지 않음</li>
 *   <li>PROCESSING: 메시지 처리 중</li>
 *   <li>COMPLETED: 메시지 처리 완료</li>
 *   <li>FAILED: 메시지 처리 실패 (재시도 가능)</li>
 * </ul>
 *
 * <p><strong>사용 범위:</strong></p>
 * <ul>
 *   <li>Pipeline 바운더리: PipelineOutbox 상태 관리</li>
 *   <li>Download 바운더리: ExternalDownloadOutbox 상태 관리</li>
 *   <li>다른 바운더리에서도 Outbox 패턴 사용 시 재사용 가능</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum OutboxStatus {
    /**
     * 대기 중 - 메시지가 생성되었으나 아직 처리되지 않음
     */
    PENDING,

    /**
     * 처리 중 - 메시지를 처리하고 있음
     */
    PROCESSING,

    /**
     * 완료 - 메시지 처리 성공
     */
    COMPLETED,

    /**
     * 실패 - 메시지 처리 실패 (재시도 가능)
     */
    FAILED
}

