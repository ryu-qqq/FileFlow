package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.application.port.out.UpdateUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ActivateUploadPolicyService 단위 테스트
 *
 * @author sangwon-ryu
 */
class ActivateUploadPolicyServiceTest {

    private ActivateUploadPolicyService service;
    private TestLoadUploadPolicyPort loadPort;
    private TestUpdateUploadPolicyPort updatePort;

    @BeforeEach
    void setUp() {
        loadPort = new TestLoadUploadPolicyPort();
        updatePort = new TestUpdateUploadPolicyPort();
        service = new ActivateUploadPolicyService(loadPort, updatePort);
    }

    @Test
    @DisplayName("정책 활성화 성공")
    void activatePolicy_success() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");
        PolicyKey policyKey = policyKeyDto.toDomain();

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                new RateLimiting(100, 1000),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
        loadPort.setPolicy(policy);

        // When
        UploadPolicyResponse response = service.activatePolicy(policyKeyDto);

        // Then
        assertNotNull(response);
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("정책 활성화 실패 - 정책 없음")
    void activatePolicy_notFound_throwsException() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");

        // When & Then
        assertThrows(PolicyNotFoundException.class, () -> service.activatePolicy(policyKeyDto));
    }

    @Test
    @DisplayName("이미 활성화된 정책 재활성화 시 예외 발생")
    void activatePolicy_alreadyActive_throwsException() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");
        PolicyKey policyKey = policyKeyDto.toDomain();

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                new RateLimiting(100, 1000),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        ).activate();
        loadPort.setPolicy(policy);

        // When & Then
        assertThrows(IllegalStateException.class, () -> service.activatePolicy(policyKeyDto));
    }

    // Test Doubles
    static class TestLoadUploadPolicyPort implements LoadUploadPolicyPort {
        private UploadPolicy policy;

        void setPolicy(UploadPolicy policy) {
            this.policy = policy;
        }

        @Override
        public Optional<UploadPolicy> loadByKey(PolicyKey policyKey) {
            if (policy != null && policy.getPolicyKey().equals(policyKey)) {
                return Optional.of(policy);
            }
            return Optional.empty();
        }

        @Override
        public Optional<UploadPolicy> loadActiveByKey(PolicyKey policyKey) {
            return Optional.empty();
        }
    }

    static class TestUpdateUploadPolicyPort implements UpdateUploadPolicyPort {
        @Override
        public UploadPolicy update(UploadPolicy uploadPolicy) {
            return uploadPolicy;
        }
    }
}
