package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.port.out.LoadUploadPolicyPort;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GetUploadPolicyService 단위 테스트
 *
 * @author sangwon-ryu
 */
class GetUploadPolicyServiceTest {

    private GetUploadPolicyService service;
    private TestLoadUploadPolicyPort loadPort;

    @BeforeEach
    void setUp() {
        loadPort = new TestLoadUploadPolicyPort();
        service = new GetUploadPolicyService(loadPort);
    }

    @Test
    @DisplayName("정책 조회 성공")
    void getPolicy_success() {
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
        UploadPolicyResponse response = service.getPolicy(policyKeyDto);

        // Then
        assertNotNull(response);
        assertEquals("b2c:CONSUMER:REVIEW", response.policyKey());
    }

    @Test
    @DisplayName("정책 조회 실패 - 정책 없음")
    void getPolicy_notFound_throwsException() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");

        // When & Then
        assertThrows(PolicyNotFoundException.class, () -> service.getPolicy(policyKeyDto));
    }

    @Test
    @DisplayName("활성 정책 조회 성공")
    void getActivePolicy_success() {
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

        // When
        UploadPolicyResponse response = service.getActivePolicy(policyKeyDto);

        // Then
        assertNotNull(response);
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("null policyKeyDto 전달 시 예외 발생")
    void getPolicy_nullDto_throwsException() {
        assertThrows(NullPointerException.class, () -> service.getPolicy(null));
    }

    @Test
    @DisplayName("비활성 정책에 대한 getActivePolicy 호출 시 예외 발생")
    void getActivePolicy_inactivePolicy_throwsException() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");
        PolicyKey policyKey = policyKeyDto.toDomain();

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                new RateLimiting(100, 1000),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        ); // 기본 상태는 비활성
        loadPort.setPolicy(policy);

        // When & Then
        assertThrows(PolicyNotFoundException.class, () -> service.getActivePolicy(policyKeyDto));
    }

    // Test Double
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
            if (policy != null && policy.getPolicyKey().equals(policyKey) && policy.isActive()) {
                return Optional.of(policy);
            }
            return Optional.empty();
        }
    }
}
