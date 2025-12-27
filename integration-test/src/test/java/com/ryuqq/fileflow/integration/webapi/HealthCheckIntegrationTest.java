package com.ryuqq.fileflow.integration.webapi;

import com.ryuqq.fileflow.integration.base.WebApiIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Health Check 통합 테스트.
 *
 * 이 테스트는 통합 테스트 인프라가 올바르게 설정되었는지 검증합니다:
 * - TestContainers (MySQL, Redis, LocalStack) 정상 기동
 * - Spring Boot 애플리케이션 정상 기동
 * - Actuator Health Endpoint 정상 응답
 */
@DisplayName("Health Check 통합 테스트")
class HealthCheckIntegrationTest extends WebApiIntegrationTest {

    @Test
    @DisplayName("애플리케이션이 정상적으로 기동되어야 한다")
    void shouldApplicationStartSuccessfully() {
        // given & when
        ResponseEntity<String> response = restTemplate.getForEntity(
            url("/actuator/health"), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Liveness Probe가 정상 응답해야 한다")
    void shouldLivenessProbeReturnOk() {
        // given & when
        ResponseEntity<String> response = restTemplate.getForEntity(
            url("/actuator/health/liveness"), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Readiness Probe가 정상 응답해야 한다")
    void shouldReadinessProbeReturnOk() {
        // given & when
        ResponseEntity<String> response = restTemplate.getForEntity(
            url("/actuator/health/readiness"), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("데이터베이스 연결이 정상이어야 한다")
    void shouldDatabaseConnectionBeHealthy() {
        // given & when
        int count = testDataHelper.count("single_upload_session");

        // then - 빈 테이블이지만 쿼리가 정상 실행되어야 함
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("DatabaseCleaner가 테이블을 정리해야 한다")
    void shouldDatabaseCleanerTruncateTables() {
        // given - 테스트 데이터 삽입
        testDataHelper.insertSingleUploadSession("test-file.jpg");
        int countBefore = testDataHelper.count("single_upload_session");
        assertThat(countBefore).isEqualTo(1);

        // when - DatabaseCleaner 실행
        databaseCleaner.clean();

        // then - 테이블이 비워져야 함
        int countAfter = testDataHelper.count("single_upload_session");
        assertThat(countAfter).isZero();
    }
}
