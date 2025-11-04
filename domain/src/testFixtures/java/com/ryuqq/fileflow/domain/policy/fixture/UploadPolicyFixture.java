package com.ryuqq.fileflow.domain.policy.fixture;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.policy.PolicyName;
import com.ryuqq.fileflow.domain.policy.Priority;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.UploadPolicy.PolicyRules;
import com.ryuqq.fileflow.domain.policy.UploadPolicy.PolicyStatus;

/**
 * UploadPolicy Test Fixture
 *
 * <p>테스트에서 UploadPolicy 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
public class UploadPolicyFixture {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private UploadPolicyFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final TenantId DEFAULT_TENANT_ID = TenantId.of(1L);
    private static final PolicyName DEFAULT_POLICY_NAME = PolicyName.of("Default Upload Policy");
    private static final Priority DEFAULT_PRIORITY = Priority.of(100);

    /**
     * 기본 UploadPolicy 생성 (활성화 상태)
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createDefault() {
        PolicyRules rules = PolicyRules.builder()
            .allowMimeTypes("image/jpeg", "image/png", "application/pdf")
            .maxFileSize(10485760L) // 10MB
            .minFileSize(1024L) // 1KB
            .allowExtensions("jpg", "png", "pdf")
            .requireScan()
            .build();

        return UploadPolicy.create(DEFAULT_TENANT_ID, DEFAULT_POLICY_NAME, rules, DEFAULT_PRIORITY);
    }

    /**
     * 특정 값으로 UploadPolicy 생성
     *
     * @param tenantId 테넌트 ID
     * @param policyName 정책 이름
     * @param rules 정책 규칙
     * @param priority 우선순위
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy create(
        TenantId tenantId,
        PolicyName policyName,
        PolicyRules rules,
        Priority priority
    ) {
        return UploadPolicy.create(tenantId, policyName, rules, priority);
    }

    /**
     * 이미지 전용 UploadPolicy 생성
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createImageOnly() {
        PolicyRules rules = PolicyRules.builder()
            .allowMimeTypes("image/jpeg", "image/png", "image/gif")
            .maxFileSize(5242880L) // 5MB
            .minFileSize(1024L)
            .allowExtensions("jpg", "jpeg", "png", "gif")
            .requireScan()
            .build();

        return UploadPolicy.create(
            DEFAULT_TENANT_ID,
            PolicyName.of("Image Only Policy"),
            rules,
            Priority.of(200)
        );
    }

    /**
     * PDF 전용 UploadPolicy 생성 (OCR 필수)
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createPdfWithOcr() {
        PolicyRules rules = PolicyRules.builder()
            .allowMimeTypes("application/pdf")
            .maxFileSize(20971520L) // 20MB
            .minFileSize(1024L)
            .allowExtensions("pdf")
            .requireScan()
            .enableOcr()
            .build();

        return UploadPolicy.create(
            DEFAULT_TENANT_ID,
            PolicyName.of("PDF with OCR Policy"),
            rules,
            Priority.of(150)
        );
    }

    /**
     * 비활성화된 UploadPolicy 생성
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createInactive() {
        UploadPolicy policy = createDefault();
        policy.deactivate();
        return policy;
    }

    /**
     * 폐기된 UploadPolicy 생성
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createDeprecated() {
        UploadPolicy policy = createDefault();
        policy.deprecate();
        return policy;
    }

    /**
     * 엄격한 보안 UploadPolicy 생성 (작은 파일만, 바이러스 스캔 필수)
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createStrictSecurity() {
        PolicyRules rules = PolicyRules.builder()
            .allowMimeTypes("text/plain", "application/pdf")
            .maxFileSize(1048576L) // 1MB
            .minFileSize(100L)
            .allowExtensions("txt", "pdf")
            .requireScan()
            .build();

        return UploadPolicy.create(
            DEFAULT_TENANT_ID,
            PolicyName.of("Strict Security Policy"),
            rules,
            Priority.of(50) // 높은 우선순위 (낮은 숫자)
        );
    }

    /**
     * 모든 파일 허용 UploadPolicy 생성 (느슨한 정책)
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createPermissive() {
        PolicyRules rules = PolicyRules.builder()
            .allowAllMimeTypes()
            .maxFileSize(1073741824L) // 1GB
            .minFileSize(1L)
            .build();

        return UploadPolicy.create(
            DEFAULT_TENANT_ID,
            PolicyName.of("Permissive Policy"),
            rules,
            Priority.of(999) // 낮은 우선순위 (높은 숫자)
        );
    }

    /**
     * DB에서 복원한 UploadPolicy 생성 (Reconstitute)
     *
     * @param id 정책 ID
     * @param tenantId 테넌트 ID
     * @param policyName 정책 이름
     * @param rules 정책 규칙
     * @param status 정책 상태
     * @param priority 우선순위
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy reconstitute(
        Long id,
        TenantId tenantId,
        PolicyName policyName,
        PolicyRules rules,
        PolicyStatus status,
        Priority priority
    ) {
        return UploadPolicy.reconstitute(
            id,
            tenantId,
            policyName,
            rules,
            status,
            priority
        );
    }

    /**
     * 기본값으로 Reconstitute된 UploadPolicy 생성
     *
     * @param id 정책 ID
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy reconstituteDefault(Long id) {
        PolicyRules rules = PolicyRules.builder()
            .allowMimeTypes("image/jpeg")
            .maxFileSize(10485760L)
            .minFileSize(1024L)
            .allowExtensions("jpg")
            .requireScan()
            .build();

        return UploadPolicy.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_POLICY_NAME,
            rules,
            PolicyStatus.ACTIVE,
            DEFAULT_PRIORITY
        );
    }

    /**
     * Builder 패턴으로 UploadPolicy 생성
     *
     * @return UploadPolicyBuilder 인스턴스
     */
    public static UploadPolicyBuilder builder() {
        return new UploadPolicyBuilder();
    }

    /**
     * UploadPolicy Builder
     */
    public static class UploadPolicyBuilder {
        private TenantId tenantId = DEFAULT_TENANT_ID;
        private PolicyName policyName = DEFAULT_POLICY_NAME;
        private Priority priority = DEFAULT_PRIORITY;
        private PolicyRules rules;
        private boolean shouldDeactivate = false;
        private boolean shouldDeprecate = false;

        public UploadPolicyBuilder tenantId(TenantId tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public UploadPolicyBuilder policyName(PolicyName policyName) {
            this.policyName = policyName;
            return this;
        }

        public UploadPolicyBuilder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public UploadPolicyBuilder rules(PolicyRules rules) {
            this.rules = rules;
            return this;
        }

        public UploadPolicyBuilder deactivate() {
            this.shouldDeactivate = true;
            return this;
        }

        public UploadPolicyBuilder deprecate() {
            this.shouldDeprecate = true;
            return this;
        }

        public UploadPolicy build() {
            PolicyRules finalRules = rules != null ? rules : PolicyRules.builder()
                .allowMimeTypes("image/jpeg")
                .maxFileSize(10485760L)
                .minFileSize(1024L)
                .allowExtensions("jpg")
                .requireScan()
                .build();

            UploadPolicy policy = UploadPolicy.create(tenantId, policyName, finalRules, priority);

            if (shouldDeactivate) {
                policy.deactivate();
            } else if (shouldDeprecate) {
                policy.deprecate();
            }

            return policy;
        }
    }
}
