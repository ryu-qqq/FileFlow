package com.ryuqq.fileflow.application;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application Layer Test Configuration
 *
 * <p><strong>목적</strong>: Application 모듈 통합 테스트를 위한 최소 설정</p>
 * <p><strong>위치</strong>: application/src/test/java/</p>
 *
 * <h3>사용 패턴</h3>
 * <ul>
 *   <li>✅ {@code @SpringBootTest} 시 자동 감지</li>
 *   <li>✅ 테스트용 최소 Spring Context 제공</li>
 *   <li>❌ 프로덕션 코드에 영향 없음</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@SpringBootApplication
public class TestConfiguration {
}
