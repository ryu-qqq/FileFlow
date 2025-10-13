package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.port.in.ValidateUploadPolicyUseCase.ValidateUploadPolicyCommand;
import com.ryuqq.fileflow.application.policy.port.out.CachePolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;
import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException;
import com.ryuqq.fileflow.domain.policy.vo.Dimension;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * UploadPolicyValidationService 통합 테스트
 *
 * Epic 1 정책 검증 로직의 통합을 검증합니다.
 *
 * @author sangwon-ryu
 */
class UploadPolicyValidationServiceTest {

    private UploadPolicyValidationService service;
    private TestLoadUploadPolicyPort loadPort;
    private TestCachePolicyPort cachePort;

    @BeforeEach
    void setUp() {
        loadPort = new TestLoadUploadPolicyPort();
        cachePort = new TestCachePolicyPort();
        service = new UploadPolicyValidationService(loadPort, cachePort);
    }

    @Nested
    @DisplayName("파일 업로드 정책 검증")
    class ValidateUploadPolicy {

        @Test
        @DisplayName("정상 파일 업로드 정책 검증 성공")
        void validateUploadPolicy_success() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createActivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    5_000_000L, // 5MB
                    3,
                    null,
                    null
            );

            // When & Then
            assertDoesNotThrow(() -> service.validate(command));
        }

        @Test
        @DisplayName("정책이 존재하지 않으면 PolicyNotFoundException 발생")
        void validateUploadPolicy_policyNotFound() {
            // Given
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    5_000_000L,
                    3,
                    null,
                    null
            );

            // When & Then
            PolicyNotFoundException exception = assertThrows(
                    PolicyNotFoundException.class,
                    () -> service.validate(command)
            );

            assertEquals("b2c:CONSUMER:REVIEW", exception.getPolicyKey());
        }

        @Test
        @DisplayName("파일 크기 초과 시 PolicyViolationException 발생")
        void validateUploadPolicy_fileSizeExceeded() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createActivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    15_000_000L, // 15MB (limit: 10MB)
                    3,
                    null,
                    null
            );

            // When & Then
            PolicyViolationException exception = assertThrows(
                    PolicyViolationException.class,
                    () -> service.validate(command)
            );

            assertEquals(PolicyViolationException.ViolationType.FILE_SIZE_EXCEEDED, exception.getViolationType());
        }

        @Test
        @DisplayName("파일 크기 초과 시 PolicyViolationException 발생 (두 번째 케이스)")
        void validateUploadPolicy_fileSizeExceeded_secondCase() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createActivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    20_000_000L, // 20MB (limit: 10MB)
                    3,
                    null,
                    null
            );

            // When & Then
            PolicyViolationException exception = assertThrows(
                    PolicyViolationException.class,
                    () -> service.validate(command)
            );

            assertEquals(PolicyViolationException.ViolationType.FILE_SIZE_EXCEEDED, exception.getViolationType());
        }

        @Test
        @DisplayName("허용되지 않은 파일 형식 시 PolicyViolationException 발생")
        void validateUploadPolicy_invalidFileFormat() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createActivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "bmp", // Not allowed (allowed: jpg, jpeg, png)
                    5_000_000L,
                    3,
                    null,
                    null
            );

            // When & Then
            PolicyViolationException exception = assertThrows(
                    PolicyViolationException.class,
                    () -> service.validate(command)
            );

            assertEquals(PolicyViolationException.ViolationType.INVALID_FORMAT, exception.getViolationType());
        }

        @Test
        @DisplayName("비활성화된 정책은 검증 실패")
        void validateUploadPolicy_inactivePolicy() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createInactivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    5_000_000L,
                    3,
                    null,
                    null
            );

            // When & Then
            PolicyNotFoundException exception = assertThrows(
                    PolicyNotFoundException.class,
                    () -> service.validate(command)
            );
        }
    }

    @Nested
    @DisplayName("Rate Limiting 검증")
    class ValidateRateLimit {

        @Test
        @DisplayName("정상 Rate Limiting 검증 성공")
        void validateRateLimit_success() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createActivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    5_000_000L,
                    3,
                    50,  // requestsPerHour limit: 100
                    500  // uploadsPerDay limit: 1000
            );

            // When & Then
            assertDoesNotThrow(() -> service.validate(command));
        }

        @Test
        @DisplayName("Rate Limit 초과 시 PolicyViolationException 발생")
        void validateRateLimit_exceeded() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createActivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    5_000_000L,
                    3,
                    150,  // requestsPerHour limit: 100 (exceeded)
                    1500  // uploadsPerDay limit: 1000 (exceeded)
            );

            // When & Then
            PolicyViolationException exception = assertThrows(
                    PolicyViolationException.class,
                    () -> service.validate(command)
            );

            assertEquals(PolicyViolationException.ViolationType.RATE_LIMIT_EXCEEDED, exception.getViolationType());
        }
    }

    @Nested
    @DisplayName("캐시 통합")
    class CacheIntegration {

        @Test
        @DisplayName("첫 조회 시 DB에서 가져와 캐시에 저장")
        void loadPolicyWithCache_firstLoad() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createActivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    5_000_000L,
                    3,
                    null,
                    null
            );

            // When
            service.validate(command);

            // Then
            assertEquals(1, loadPort.getLoadCount());
            assertEquals(1, cachePort.getSaveCount());
        }

        @Test
        @DisplayName("두 번째 조회 시 캐시에서 가져옴")
        void loadPolicyWithCache_secondLoad() {
            // Given
            PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
            UploadPolicy policy = createActivePolicy(policyKey);
            loadPort.save(policy);

            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "b2c",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // fileFormat (optional)
                    5_000_000L,
                    3,
                    null,
                    null
            );

            // When - 첫 번째 호출 (DB 조회 + 캐시 저장)
            service.validate(command);
            int firstLoadCount = loadPort.getLoadCount();
            int firstSaveCount = cachePort.getSaveCount();

            // When - 두 번째 호출 (캐시에서 조회)
            service.validate(command);

            // Then
            assertEquals(firstLoadCount, loadPort.getLoadCount()); // DB 조회 증가 없음
            assertEquals(firstSaveCount, cachePort.getSaveCount()); // 추가 캐시 저장 없음
        }
    }

    // ========== Test Fixtures ==========

    private UploadPolicy createActivePolicy(PolicyKey policyKey) {
        ImagePolicy imagePolicy = new ImagePolicy(
                10, // maxFileSizeMB
                10, // maxFileCount
                List.of("jpg", "jpeg", "png"),
                Dimension.of(4096, 4096)
        );

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                FileTypePolicies.of(imagePolicy, null, null, null, null),
                new RateLimiting(100, 1000),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusYears(1)
        );

        return policy.activate();
    }

    private UploadPolicy createInactivePolicy(PolicyKey policyKey) {
        ImagePolicy imagePolicy = ImagePolicy.createDefault();

        return UploadPolicy.create(
                policyKey,
                FileTypePolicies.of(imagePolicy, null, null, null, null),
                new RateLimiting(100, 1000),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusYears(1)
        );
    }

    // ========== Test Doubles ==========

    private static class TestLoadUploadPolicyPort implements LoadUploadPolicyPort {
        private UploadPolicy policy;
        private int loadCount = 0;

        public void save(UploadPolicy policy) {
            this.policy = policy;
        }

        @Override
        public Optional<UploadPolicy> loadByKey(PolicyKey policyKey) {
            loadCount++;
            return Optional.ofNullable(policy);
        }

        @Override
        public Optional<UploadPolicy> loadActiveByKey(PolicyKey policyKey) {
            loadCount++;
            if (policy != null && policy.isActive()) {
                return Optional.of(policy);
            }
            return Optional.empty();
        }

        public int getLoadCount() {
            return loadCount;
        }
    }

    private static class TestCachePolicyPort implements CachePolicyPort {
        private UploadPolicy cachedPolicy;
        private int saveCount = 0;

        @Override
        public Optional<UploadPolicy> get(PolicyKey policyKey) {
            return Optional.ofNullable(cachedPolicy);
        }

        @Override
        public void put(UploadPolicy policy) {
            saveCount++;
            this.cachedPolicy = policy;
        }

        @Override
        public void evict(PolicyKey policyKey) {
            this.cachedPolicy = null;
        }

        @Override
        public void evictAll() {
            this.cachedPolicy = null;
        }

        public int getSaveCount() {
            return saveCount;
        }
    }
}
