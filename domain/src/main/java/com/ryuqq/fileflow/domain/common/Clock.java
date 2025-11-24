package com.ryuqq.crawlinghub.domain.common;

import java.time.Instant;

/**
 * Clock - 시간 추상화 인터페이스
 *
 * <p>테스트 가능한 시간 관리를 위한 추상화 계층입니다.
 *
 * <p><strong>용도:</strong>
 *
 * <ul>
 *   <li>Aggregate Root의 생성/수정 시간 관리
 *   <li>테스트에서 시간 제어 (고정된 시간 반환)
 *   <li>운영 환경에서 실제 시간 사용
 * </ul>
 *
 * <p><strong>구현 예시:</strong>
 *
 * <ul>
 *   <li>Production: {@code () -> Instant.now()}
 *   <li>Test: {@code () -> Instant.parse("2025-11-24T00:00:00Z")}
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@FunctionalInterface
public interface Clock {

    /**
     * 현재 시간 반환
     *
     * @return 현재 시간 (Instant)
     * @author development-team
     * @since 1.0.0
     */
    Instant now();
}
