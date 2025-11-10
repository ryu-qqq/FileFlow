package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.command.CheckRateLimitCommand;
import com.ryuqq.fileflow.application.upload.dto.response.RateLimitResponse;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * CheckUploadRateLimitService 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>execute() - Rate Limit 확인</li>
 *   <li>허용 시나리오 (currentCount < maxConcurrentPerTenant)</li>
 *   <li>거부 시나리오 (currentCount >= maxConcurrentPerTenant)</li>
 *   <li>경계 조건 (currentCount == maxConcurrentPerTenant)</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Rate Limit 로직 정확성</li>
 *   <li>Response 생성 정확성 (currentCount, remaining, allowed)</li>
 *   <li>Port 위임 정확성</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CheckUploadRateLimitService 단위 테스트")
class CheckUploadRateLimitServiceTest {

    @Mock
    private LoadUploadSessionPort loadUploadSessionPort;

    private CheckUploadRateLimitService service;

    private static final int MAX_CONCURRENT_PER_TENANT = 10;
    private Long tenantId;

    @BeforeEach
    void setUp() {
        tenantId = 1L;
        // 수동으로 인스턴스 생성 (생성자가 @Value 어노테이션을 사용하므로 @InjectMocks 사용 불가)
        service = new CheckUploadRateLimitService(
            loadUploadSessionPort,
            MAX_CONCURRENT_PER_TENANT
        );
    }

    @Nested
    @DisplayName("execute 메서드 - 허용 시나리오")
    class AllowedScenarioTests {

        @Test
        @DisplayName("execute_WithZeroCurrentCount_ShouldAllowUpload - 진행 중인 세션 없을 시 허용")
        void execute_WithZeroCurrentCount_ShouldAllowUpload() {
            // Given - 진행 중인 세션 없음
            CheckRateLimitCommand command = CheckRateLimitCommand.of(tenantId);
            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenantId), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(0L);

            // When
            RateLimitResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.currentCount()).isEqualTo(0L);
            assertThat(response.maxAllowed()).isEqualTo(MAX_CONCURRENT_PER_TENANT);
            assertThat(response.remaining()).isEqualTo(10L);
            assertThat(response.allowed()).isTrue();

            verify(loadUploadSessionPort).countByTenantIdAndStatus(eq(tenantId), eq(SessionStatus.IN_PROGRESS));
        }

        @Test
        @DisplayName("execute_WithCurrentCountBelowLimit_ShouldAllowUpload - 제한보다 적은 세션 진행 중일 시 허용")
        void execute_WithCurrentCountBelowLimit_ShouldAllowUpload() {
            // Given - 진행 중인 세션 5개 (< 10)
            CheckRateLimitCommand command = CheckRateLimitCommand.of(tenantId);
            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenantId), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(5L);

            // When
            RateLimitResponse response = service.execute(command);

            // Then
            assertThat(response.currentCount()).isEqualTo(5L);
            assertThat(response.remaining()).isEqualTo(5L);
            assertThat(response.allowed()).isTrue();
        }

        @Test
        @DisplayName("execute_WithOneBelowLimit_ShouldAllowUpload - 제한보다 1개 적은 세션 진행 중일 시 허용")
        void execute_WithOneBelowLimit_ShouldAllowUpload() {
            // Given - 진행 중인 세션 9개 (< 10)
            CheckRateLimitCommand command = CheckRateLimitCommand.of(tenantId);
            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenantId), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(9L);

            // When
            RateLimitResponse response = service.execute(command);

            // Then
            assertThat(response.currentCount()).isEqualTo(9L);
            assertThat(response.remaining()).isEqualTo(1L);
            assertThat(response.allowed()).isTrue();
        }
    }

    @Nested
    @DisplayName("execute 메서드 - 거부 시나리오")
    class DeniedScenarioTests {

        @Test
        @DisplayName("execute_WithCurrentCountAtLimit_ShouldDenyUpload - 제한과 같은 세션 진행 중일 시 거부")
        void execute_WithCurrentCountAtLimit_ShouldDenyUpload() {
            // Given - 진행 중인 세션 10개 (== 10, 경계 조건)
            CheckRateLimitCommand command = CheckRateLimitCommand.of(tenantId);
            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenantId), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(10L);

            // When
            RateLimitResponse response = service.execute(command);

            // Then
            assertThat(response.currentCount()).isEqualTo(10L);
            assertThat(response.remaining()).isEqualTo(0L);
            assertThat(response.allowed()).isFalse();
        }

        @Test
        @DisplayName("execute_WithCurrentCountExceedsLimit_ShouldDenyUpload - 제한 초과 세션 진행 중일 시 거부")
        void execute_WithCurrentCountExceedsLimit_ShouldDenyUpload() {
            // Given - 진행 중인 세션 15개 (> 10, 비정상 상태)
            CheckRateLimitCommand command = CheckRateLimitCommand.of(tenantId);
            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenantId), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(15L);

            // When
            RateLimitResponse response = service.execute(command);

            // Then
            assertThat(response.currentCount()).isEqualTo(15L);
            assertThat(response.remaining()).isEqualTo(0L);  // 음수 방지 (Math.max 사용)
            assertThat(response.allowed()).isFalse();
        }
    }

    @Nested
    @DisplayName("execute 메서드 - 여러 Tenant 시나리오")
    class MultiTenantScenarioTests {

        @Test
        @DisplayName("execute_WithDifferentTenants_ShouldIsolateLimits - 다른 Tenant는 독립적인 Rate Limit 적용")
        void execute_WithDifferentTenants_ShouldIsolateLimits() {
            // Given - Tenant 1 (5개), Tenant 2 (10개)
            Long tenant1 = 1L;
            Long tenant2 = 2L;

            CheckRateLimitCommand command1 = CheckRateLimitCommand.of(tenant1);
            CheckRateLimitCommand command2 = CheckRateLimitCommand.of(tenant2);

            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenant1), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(5L);
            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenant2), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(10L);

            // When
            RateLimitResponse response1 = service.execute(command1);
            RateLimitResponse response2 = service.execute(command2);

            // Then
            // Tenant 1: 허용 (5 < 10)
            assertThat(response1.tenantId()).isEqualTo(tenant1);
            assertThat(response1.currentCount()).isEqualTo(5L);
            assertThat(response1.allowed()).isTrue();

            // Tenant 2: 거부 (10 == 10)
            assertThat(response2.tenantId()).isEqualTo(tenant2);
            assertThat(response2.currentCount()).isEqualTo(10L);
            assertThat(response2.allowed()).isFalse();
        }
    }

    @Nested
    @DisplayName("CheckRateLimitCommand 유효성 검증")
    class CommandValidationTests {

        @Test
        @DisplayName("command_WithNullTenantId_ShouldThrowException - null tenantId 시 예외")
        void command_WithNullTenantId_ShouldThrowException() {
            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> CheckRateLimitCommand.of(null)
            );
        }
    }

    @Nested
    @DisplayName("RateLimitResponse 검증")
    class ResponseValidationTests {

        @Test
        @DisplayName("response_ShouldContainAllRequiredFields - 응답에 모든 필수 필드 포함")
        void response_ShouldContainAllRequiredFields() {
            // Given
            CheckRateLimitCommand command = CheckRateLimitCommand.of(tenantId);
            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenantId), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(3L);

            // When
            RateLimitResponse response = service.execute(command);

            // Then - 모든 필드 검증
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.currentCount()).isEqualTo(3L);
            assertThat(response.maxAllowed()).isEqualTo(MAX_CONCURRENT_PER_TENANT);
            assertThat(response.remaining()).isEqualTo(7L);
            assertThat(response.allowed()).isTrue();
        }

        @Test
        @DisplayName("response_WithNegativeRemaining_ShouldReturnZero - 음수 remaining 시 0 반환")
        void response_WithNegativeRemaining_ShouldReturnZero() {
            // Given - 제한 초과 상태
            CheckRateLimitCommand command = CheckRateLimitCommand.of(tenantId);
            given(loadUploadSessionPort.countByTenantIdAndStatus(eq(tenantId), eq(SessionStatus.IN_PROGRESS)))
                .willReturn(20L);  // 20 > 10

            // When
            RateLimitResponse response = service.execute(command);

            // Then - remaining은 Math.max(0, ...) 사용하여 음수 방지
            assertThat(response.remaining()).isEqualTo(0L);
            assertThat(response.remaining()).isNotNegative();
        }
    }
}
