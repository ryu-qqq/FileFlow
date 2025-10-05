package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.port.out.DeleteUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * DeleteUploadPolicyService 단위 테스트
 *
 * @author sangwon-ryu
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
        value = "UwF",
        justification = "service 필드는 @BeforeEach setUp()에서 초기화됩니다."
)
class DeleteUploadPolicyServiceTest {

    private DeleteUploadPolicyService service;
    private TestLoadUploadPolicyPort loadPort;
    private TestDeleteUploadPolicyPort deletePort;

    @BeforeEach
    void setUp() {
        loadPort = new TestLoadUploadPolicyPort();
        deletePort = new TestDeleteUploadPolicyPort();
        service = new DeleteUploadPolicyService(loadPort, deletePort);
    }

    @Test
    @DisplayName("비활성 정책 삭제 성공")
    void deletePolicy_success() {
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

        // When & Then
        assertDoesNotThrow(() -> service.deletePolicy(policyKeyDto));
    }

    @Test
    @DisplayName("활성 정책 삭제 시 예외 발생")
    void deletePolicy_activePolicy_throwsException() {
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
        assertThrows(IllegalStateException.class, () -> service.deletePolicy(policyKeyDto));
    }

    @Test
    @DisplayName("존재하지 않는 정책 삭제 시 예외 발생")
    void deletePolicy_notFound_throwsException() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");

        // When & Then
        assertThrows(PolicyNotFoundException.class, () -> service.deletePolicy(policyKeyDto));
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

    static class TestDeleteUploadPolicyPort implements DeleteUploadPolicyPort {
        @Override
        public void delete(PolicyKey policyKey) {
            // 삭제 성공
        }
    }
}
