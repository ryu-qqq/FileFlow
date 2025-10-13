package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.dto.CreateUploadPolicyCommand;
import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.SaveUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * CreateUploadPolicyService 단위 테스트
 *
 * @author sangwon-ryu
 */
class CreateUploadPolicyServiceTest {

    private CreateUploadPolicyService service;
    private TestLoadUploadPolicyPort loadPort;
    private TestSaveUploadPolicyPort savePort;

    @BeforeEach
    void setUp() {
        loadPort = new TestLoadUploadPolicyPort();
        savePort = new TestSaveUploadPolicyPort();
        service = new CreateUploadPolicyService(loadPort, savePort);
    }

    @Test
    @DisplayName("정책 생성 성공")
    void createPolicy_success() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");
        ImagePolicy imagePolicy = ImagePolicy.createDefault();
        CreateUploadPolicyCommand command = new CreateUploadPolicyCommand(
                policyKeyDto,
                imagePolicy,
                null,
                null,
                null,
                null,
                100,
                1000,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );

        // When
        UploadPolicyResponse response = service.createPolicy(command);

        // Then
        assertNotNull(response);
        assertEquals("b2c:CONSUMER:REVIEW", response.policyKey());
        assertEquals(1, response.version());
        assertFalse(response.isActive());
    }

    @Test
    @DisplayName("중복 정책 생성 시 예외 발생")
    void createPolicy_duplicateKey_throwsException() {
        // Given
        PolicyKeyDto policyKeyDto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");
        PolicyKey policyKey = policyKeyDto.toDomain();
        ImagePolicy imagePolicy = ImagePolicy.createDefault();

        // 기존 정책 설정
        UploadPolicy existingPolicy = UploadPolicy.create(
                policyKey,
                FileTypePolicies.of(imagePolicy, null, null, null, null),
                new RateLimiting(100, 1000),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
        loadPort.setPolicy(existingPolicy);

        CreateUploadPolicyCommand command = new CreateUploadPolicyCommand(
                policyKeyDto,
                imagePolicy,
                null,
                null,
                null,
                null,
                100,
                1000,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );

        // When & Then
        assertThrows(IllegalStateException.class, () -> service.createPolicy(command));
    }

    @Test
    @DisplayName("null command 전달 시 예외 발생")
    void createPolicy_nullCommand_throwsException() {
        assertThrows(NullPointerException.class, () -> service.createPolicy(null));
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
            if (policy != null && policy.getPolicyKey().equals(policyKey) && policy.isActive()) {
                return Optional.of(policy);
            }
            return Optional.empty();
        }
    }

    static class TestSaveUploadPolicyPort implements SaveUploadPolicyPort {
        @Override
        public UploadPolicy save(UploadPolicy uploadPolicy) {
            return uploadPolicy;
        }
    }
}
