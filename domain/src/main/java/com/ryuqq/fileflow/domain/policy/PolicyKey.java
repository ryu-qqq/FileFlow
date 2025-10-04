package com.ryuqq.fileflow.domain.policy;

import java.util.Objects;

/**
 * 정책을 식별하는 키를 나타내는 Value Object
 * 형식: {tenantId}:{userType}:{serviceType}
 * 예시: b2c:CONSUMER:REVIEW, b2c:SELLER:PRODUCT
 */
public class PolicyKey {

    private final String tenantId;
    private final String userType;
    private final String serviceType;

    private PolicyKey(String tenantId, String userType, String serviceType) {
        this.tenantId = tenantId;
        this.userType = userType;
        this.serviceType = serviceType;
    }

    public static PolicyKey of(String tenantId, String userType, String serviceType) {
        validateNotBlank(tenantId, "tenantId");
        validateNotBlank(userType, "userType");
        validateNotBlank(serviceType, "serviceType");

        return new PolicyKey(tenantId, userType, serviceType);
    }

    private static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserType() {
        return userType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getValue() {
        return tenantId + ":" + userType + ":" + serviceType;
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyKey policyKey = (PolicyKey) o;
        return Objects.equals(tenantId, policyKey.tenantId) &&
               Objects.equals(userType, policyKey.userType) &&
               Objects.equals(serviceType, policyKey.serviceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, userType, serviceType);
    }
}
