package com.ryuqq.fileflow;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;

/**
 * adapter-out:persistence-mysql 모듈의 테스트 전용 Spring Boot Configuration
 *
 * <p><strong>목적:</strong></p>
 * <ul>
 *   <li>@DataJpaTest를 사용하는 테스트들이 @SpringBootConfiguration을 찾을 수 있도록 제공</li>
 *   <li>Flyway 자동 설정 비활성화 (테스트에서는 H2 사용)</li>
 * </ul>
 *
 * <p><strong>작동 원리:</strong></p>
 * <ul>
 *   <li>Spring Boot Test는 패키지를 상위로 올라가며 @SpringBootConfiguration을 찾음</li>
 *   <li>이 클래스는 com.ryuqq.fileflow 패키지에 위치하여 모든 하위 테스트가 찾을 수 있음</li>
 *   <li>@DataJpaTest는 이 설정을 사용하여 JPA 테스트 컨텍스트를 초기화</li>
 *   <li>@DataJpaTest가 자동으로 JPA 관련 Bean만 로드하므로 ComponentScan 불필요</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = FlywayAutoConfiguration.class)
public class TestConfig {
    // Empty configuration class for test context
    // @DataJpaTest가 JPA 관련 Bean을 자동으로 로드
}
