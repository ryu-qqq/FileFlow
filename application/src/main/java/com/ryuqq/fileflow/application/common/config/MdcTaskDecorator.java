package com.ryuqq.fileflow.application.common.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * MDC 컨텍스트를 비동기 태스크로 전파하는 TaskDecorator.
 *
 * <p>@Async 메서드 실행 시 호출 스레드의 MDC 컨텍스트(traceId, userId, tenantId 등)를 비동기 스레드로 복사합니다.
 *
 * <p><strong>사용 목적</strong>:
 *
 * <ul>
 *   <li>비동기 처리에서도 분산 추적(traceId) 연속성 유지
 *   <li>로그에서 요청 컨텍스트 정보 유지
 *   <li>Sentry 등 모니터링 도구에서 컨텍스트 연결
 * </ul>
 *
 * <p><strong>MDC 전파 항목</strong>:
 *
 * <ul>
 *   <li>traceId - 분산 추적 식별자
 *   <li>userId - 사용자 식별자
 *   <li>tenantId - 테넌트 식별자
 *   <li>requestId - 요청 고유 ID
 *   <li>기타 모든 MDC 항목
 * </ul>
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 호출 스레드의 MDC 컨텍스트 복사
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // 비동기 스레드에 MDC 컨텍스트 설정
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // 태스크 완료 후 MDC 정리 (스레드 재사용 시 오염 방지)
                MDC.clear();
            }
        };
    }
}
