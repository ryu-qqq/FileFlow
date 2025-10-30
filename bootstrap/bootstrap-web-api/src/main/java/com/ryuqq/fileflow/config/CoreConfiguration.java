package com.ryuqq.fileflow.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;

/**
 * Core Configuration - 핵심 Bean 설정
 *
 * <p>Bootstrap 모듈의 핵심 Bean들을 정의합니다.</p>
 * <p>실행 가능한 Application의 최상위 모듈로서 모든 공통 Bean을 관리합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Bootstrap이 최상위 실행 모듈 - 핵심 Bean은 여기서 관리</li>
 *   <li>✅ 하위 모듈(Application, Adapter)은 Bean 정의 분산 금지</li>
 *   <li>✅ 전역 Bean (Clock, ObjectMapper, DomainService)은 Bootstrap에 집중</li>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 * </ul>
 *
 * <p><strong>Bean 목록:</strong></p>
 * <ul>
 *   <li>{@link Clock} - 시간 제어 및 테스트 용이성</li>
 *   <li>{@link ObjectMapper} - 전역 JSON 직렬화/역직렬화</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 */
@Configuration
public class CoreConfiguration {

    /**
     * Clock Bean 생성
     *
     * <p>시스템 기본 타임존의 Clock을 반환합니다.</p>
     * <p>테스트에서는 @MockBean이나 @TestConfiguration으로 오버라이드할 수 있습니다.</p>
     *
     * <p><strong>사용 예시:</strong></p>
     * <pre>{@code
     * // Domain 객체에서 주입받아 사용
     * public Setting(Clock clock) {
     *     this.createdAt = LocalDateTime.now(clock);
     * }
     * }</pre>
     *
     * @return Clock 인스턴스 (systemDefaultZone)
     * @author ryu-qqq
     * @since 2025-10-29
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /**
     * ObjectMapper Bean 생성 (전역 JSON 직렬화/역직렬화)
     *
     * <p>애플리케이션 전역에서 사용할 ObjectMapper를 생성합니다.</p>
     *
     * <p><strong>설정 내용:</strong></p>
     * <ul>
     *   <li>JavaTimeModule: Java 8 Time API 지원 (LocalDateTime, Instant 등)</li>
     *   <li>WRITE_DATES_AS_TIMESTAMPS: false → ISO-8601 문자열로 저장</li>
     *   <li>FAIL_ON_UNKNOWN_PROPERTIES: false → 역직렬화 시 알 수 없는 필드 무시</li>
     *   <li>날짜 형식: "2025-10-29T15:30:00" (사람이 읽기 쉬움)</li>
     * </ul>
     *
     * <p><strong>사용처:</strong></p>
     * <ul>
     *   <li>REST API 요청/응답 직렬화 (Spring MVC)</li>
     *   <li>Redis 캐시 직렬화 (RedisConfig에서 재사용)</li>
     *   <li>JSON 스키마 검증 (SimpleSchemaValidator)</li>
     *   <li>테스트 데이터 생성 및 검증</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>@Primary: 여러 ObjectMapper Bean이 있을 때 기본으로 사용</li>
     *   <li>Redis는 별도 설정 필요 (타입 힌트 활성화)</li>
     * </ul>
     *
     * <p><strong>직렬화 예시:</strong></p>
     * <pre>{@code
     * // LocalDateTime 직렬화
     * LocalDateTime now = LocalDateTime.now();
     * // JSON: "2025-10-29T15:30:00"
     *
     * // Instant 직렬화
     * Instant instant = Instant.now();
     * // JSON: "2025-10-29T06:30:00Z"
     * }</pre>
     *
     * @return ObjectMapper (Java 8 Time 지원, 전역 사용)
     * @author ryu-qqq
     * @since 2025-10-29
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 Time API 지원
        mapper.registerModule(new JavaTimeModule());

        // 날짜를 ISO-8601 문자열로 저장 (타임스탬프 숫자 대신)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 역직렬화 시 알 수 없는 필드 무시 (하위 호환성)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}
