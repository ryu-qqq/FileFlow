package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.dto.UpdateUploadPolicyCommand;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * UpdateUploadPolicyService 단위 테스트
 *
 * @author sangwon-ryu
 */
class UpdateUploadPolicyServiceTest {

    private UpdateUploadPolicyService service;
    private TestLoadUploadPolicyPort loadPort;
    private TestUpdateUploadPolicyPort updatePort;

    @BeforeEach
    void setUp() {
        loadPort = new TestLoadUploadPolicyPort();
        updatePort = new TestUpdateUploadPolicyPort();
        service = new UpdateUploadPolicyService(loadPort, updatePort);
    }

    @Test
    @DisplayName("정책 업데이트 성공")
    void updatePolicy_success() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");
        PolicyKey policyKey = policyKeyDto.toDomain();

        UploadPolicy existingPolicy = UploadPolicy.create(
                policyKey,
                FileTypePolicies.of(ImagePolicy.createDefault(), null, null, null),
                new RateLimiting(100, 1000),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
        loadPort.setPolicy(existingPolicy);

        UpdateUploadPolicyCommand command = new UpdateUploadPolicyCommand(
                policyKeyDto,
                ImagePolicy.createDefault(),
                null,
                null,
                null,
                "admin"
        );

        // When
        UploadPolicyResponse response = service.updatePolicy(command);

        // Then
        assertNotNull(response);
        assertEquals(2, response.version()); // 버전 증가 확인
    }

    @Test
    @DisplayName("존재하지 않는 정책 업데이트 시 예외 발생")
    void updatePolicy_notFound_throwsException() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");
        UpdateUploadPolicyCommand command = new UpdateUploadPolicyCommand(
                policyKeyDto,
                ImagePolicy.createDefault(),
                null,
                null,
                null,
                "admin"
        );

        // When & Then
        assertThrows(PolicyNotFoundException.class, () -> service.updatePolicy(command));
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
