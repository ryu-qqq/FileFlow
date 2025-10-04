package com.ryuqq.fileflow.application.policy.dto;

import com.ryuqq.fileflow.domain.policy.PolicyKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PolicyKeyDto 단위 테스트
 *
 * @author sangwon-ryu
 */
class PolicyKeyDtoTest {

    @Test
    @DisplayName("DTO를 Domain으로 변환")
    void toDomain() {
        // Given
        PolicyKeyDto dto = new PolicyKeyDto("b2c", "CONSUMER", "REVIEW");

        // When
        PolicyKey policyKey = dto.toDomain();

        // Then
        assertEquals("b2c", policyKey.getTenantId());
        assertEquals("CONSUMER", policyKey.getUserType());
        assertEquals("REVIEW", policyKey.getServiceType());
        assertEquals("b2c:CONSUMER:REVIEW", policyKey.getValue());
    }

    @Test
    @DisplayName("Domain으로부터 DTO 생성")
    void from() {
        // Given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");

        // When
        PolicyKeyDto dto = PolicyKeyDto.from(policyKey);

        // Then
        assertEquals("b2c", dto.tenantId());
        assertEquals("CONSUMER", dto.userType());
        assertEquals("REVIEW", dto.serviceType());
    }
}
