package com.ryuqq.fileflow.application.download.port.out;

import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;

/**
 * External Download Outbox Command Port (CQRS - Command Side)
 * 명령(생성/수정/삭제) 전용 Port 인터페이스
 *
 * <p><strong>CQRS 패턴 적용:</strong></p>
 * <ul>
 *   <li>명령 메서드만 포함 (쓰기 전용)</li>
 *   <li>트랜잭션 필수</li>
 *   <li>Event 발행 가능</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface ExternalDownloadOutboxCommandPort {

    /**
     * 아웃박스 저장 또는 업데이트
     *
     * @param outbox 저장할 아웃박스
     * @return 저장된 아웃박스
     */
    ExternalDownloadOutbox save(ExternalDownloadOutbox outbox);

    /**
     * 아웃박스 삭제 (처리 완료 후)
     *
     * @param outboxId 삭제할 아웃박스 ID
     */
    void deleteById(Long outboxId);

    /**
     * 오래된 처리 완료 메시지 일괄 삭제
     *
     * @param beforeDate 이 날짜 이전의 PROCESSED 메시지 삭제
     * @return 삭제된 메시지 개수
     */
    int deleteProcessedMessagesBefore(java.time.LocalDateTime beforeDate);
}