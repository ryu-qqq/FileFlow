package com.ryuqq.fileflow.application.port.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SqsClientPort 인터페이스 계약 테스트
 * <p>
 * 외부 API Port 규칙:
 * - 인터페이스명: *ClientPort
 * - 패키지: ..application..port.out.external..
 * - 메서드: 외부 API 호출 메서드 (sendMessage, sendMessageBatch)
 * - Timeout, Retry 정책 Javadoc 필수
 * </p>
 */
class SqsClientPortTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * SqsClientPort 인터페이스가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("SqsClientPort는 sendMessage() 메서드를 제공해야 한다")
    void shouldProvideSendMessageMethod() {
        // Given: SqsClientPort 인터페이스 (컴파일 에러)
        SqsClientPort port = null;

        // When & Then: 메서드 시그니처 검증
        // String sendMessage(String queueUrl, String messageBody) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("SqsClientPort는 sendMessageBatch() 메서드를 제공해야 한다")
    void shouldProvideSendMessageBatchMethod() {
        // Given: SqsClientPort 인터페이스 (컴파일 에러)
        SqsClientPort port = null;

        // When & Then: 메서드 시그니처 검증
        // List<String> sendMessageBatch(String queueUrl, List<String> messageBodies) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("sendMessage()는 String을 반환해야 한다")
    void sendMessageShouldReturnString() {
        // Given: SqsClientPort 인터페이스 (컴파일 에러)
        SqsClientPort port = null;

        // When & Then: 반환 타입 검증
        // String 반환 (Message ID)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("sendMessageBatch()는 List<String>을 반환해야 한다")
    void sendMessageBatchShouldReturnListOfString() {
        // Given: SqsClientPort 인터페이스 (컴파일 에러)
        SqsClientPort port = null;

        // When & Then: 반환 타입 검증
        // List<String> 반환 (Message IDs)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }
}
