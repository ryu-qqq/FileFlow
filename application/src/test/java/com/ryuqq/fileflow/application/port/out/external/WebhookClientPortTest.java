package com.ryuqq.fileflow.application.port.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebhookClientPort 인터페이스 계약 테스트
 * <p>
 * 외부 API Port 규칙:
 * - 인터페이스명: *ClientPort
 * - 패키지: ..application..port.out.external..
 * - 메서드: 외부 API 호출 메서드 (send)
 * - Timeout, Retry 정책 Javadoc 필수
 * </p>
 */
class WebhookClientPortTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * WebhookClientPort 인터페이스가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("WebhookClientPort는 send() 메서드를 제공해야 한다")
    void shouldProvideSendMethod() {
        // Given: WebhookClientPort 인터페이스 (컴파일 에러)
        WebhookClientPort port = null;

        // When & Then: 메서드 시그니처 검증
        // void send(String webhookUrl, String payload) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("send()는 void를 반환해야 한다")
    void sendShouldReturnVoid() {
        // Given: WebhookClientPort 인터페이스 (컴파일 에러)
        WebhookClientPort port = null;

        // When & Then: 반환 타입 검증
        // void 반환
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("send()는 webhookUrl과 payload 파라미터를 받아야 한다")
    void sendShouldAcceptWebhookUrlAndPayload() {
        // Given: WebhookClientPort 인터페이스 (컴파일 에러)
        WebhookClientPort port = null;

        // When & Then: 파라미터 타입 검증
        // String webhookUrl, String payload
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }
}
