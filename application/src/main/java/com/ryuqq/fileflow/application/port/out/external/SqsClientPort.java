package com.ryuqq.fileflow.application.port.out.external;

import java.util.List;

/**
 * SQS Client Port (Outbound Port - External API)
 * <p>
 * 외부 API Port 규칙:
 * - 인터페이스명: *ClientPort
 * - 패키지: ..application..port.out.external..
 * - 메서드: 외부 API 호출 메서드
 * - Timeout, Retry 정책 명시 필수
 * </p>
 * <p>
 * Application Layer에서 AWS SQS 외부 서비스로의 메시지 전송을 위한 Port입니다.
 * </p>
 */
public interface SqsClientPort {

    /**
     * 단일 메시지 전송
     * <p>
     * Timeout: 30초
     * Retry: 3회 (Exponential Backoff: 100ms, 200ms, 400ms)
     * </p>
     *
     * @param queueUrl SQS 큐 URL
     * @param messageBody 메시지 본문 (JSON 형식)
     * @return Message ID (SQS가 생성한 메시지 식별자)
     */
    String sendMessage(String queueUrl, String messageBody);

    /**
     * 배치 메시지 전송 (최대 10개)
     * <p>
     * Timeout: 1분
     * Retry: 3회 (Exponential Backoff: 100ms, 200ms, 400ms)
     * </p>
     *
     * @param queueUrl SQS 큐 URL
     * @param messageBodies 메시지 본문 목록 (JSON 형식, 최대 10개)
     * @return Message IDs (SQS가 생성한 메시지 식별자 목록)
     */
    List<String> sendMessageBatch(String queueUrl, List<String> messageBodies);
}
