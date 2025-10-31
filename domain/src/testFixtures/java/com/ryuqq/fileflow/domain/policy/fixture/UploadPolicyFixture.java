package com.ryuqq.fileflow.domain.policy.fixture;

import com.ryuqq.fileflow.domain.policy.UploadPolicy;

import java.time.LocalDateTime;
import java.util.List;

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

    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final String DEFAULT_POLICY_NAME = "Default Upload Policy";

    /**
     * 기본 UploadPolicy 생성 (활성화 상태)
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createDefault() {
        UploadPolicy.PolicyRules rules = UploadPolicy.PolicyRules.builder()
            .allowedMimeTypes(List.of("image/jpeg", "image/png", "application/pdf"))
            .maxFileSize(10485760L) // 10MB
            .minFileSize(1024L) // 1KB
            .allowedExtensions(List.of(".jpg", ".png", ".pdf"))
            .requireVirusScan(true)
            .requireOcr(false)
            .build();

        return UploadPolicy.create(DEFAULT_TENANT_ID, DEFAULT_POLICY_NAME, rules);
    }

    /**
     * 특정 값으로 UploadPolicy 생성
     *
     * @param tenantId 테넌트 ID
     * @param policyName 정책 이름
     * @param rules 정책 규칙
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy create(Long tenantId, String policyName, UploadPolicy.PolicyRules rules) {
        return UploadPolicy.create(tenantId, policyName, rules);
    }

    /**
     * 이미지 전용 UploadPolicy 생성
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createImageOnly() {
        UploadPolicy.PolicyRules rules = UploadPolicy.PolicyRules.builder()
            .allowedMimeTypes(List.of("image/jpeg", "image/png", "image/gif"))
            .maxFileSize(5242880L) // 5MB
            .minFileSize(1024L)
            .allowedExtensions(List.of(".jpg", ".jpeg", ".png", ".gif"))
            .requireVirusScan(true)
            .requireOcr(false)
            .build();

        return UploadPolicy.create(DEFAULT_TENANT_ID, "Image Only Policy", rules);
    }

    /**
     * PDF 전용 UploadPolicy 생성 (OCR 필수)
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createPdfWithOcr() {
        UploadPolicy.PolicyRules rules = UploadPolicy.PolicyRules.builder()
            .allowedMimeTypes(List.of("application/pdf"))
            .maxFileSize(20971520L) // 20MB
            .minFileSize(1024L)
            .allowedExtensions(List.of(".pdf"))
            .requireVirusScan(true)
            .requireOcr(true)
            .build();

        return UploadPolicy.create(DEFAULT_TENANT_ID, "PDF with OCR Policy", rules);
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
     * 엄격한 보안 UploadPolicy 생성 (작은 파일만, 바이러스 스캔 필수)
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createStrictSecurity() {
        UploadPolicy.PolicyRules rules = UploadPolicy.PolicyRules.builder()
            .allowedMimeTypes(List.of("text/plain", "application/pdf"))
            .maxFileSize(1048576L) // 1MB
            .minFileSize(100L)
            .allowedExtensions(List.of(".txt", ".pdf"))
            .requireVirusScan(true)
            .requireOcr(false)
            .build();

        return UploadPolicy.create(DEFAULT_TENANT_ID, "Strict Security Policy", rules);
    }

    /**
     * 모든 파일 허용 UploadPolicy 생성 (느슨한 정책)
     *
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy createPermissive() {
        UploadPolicy.PolicyRules rules = UploadPolicy.PolicyRules.builder()
            .allowedMimeTypes(List.of("*/*"))
            .maxFileSize(1073741824L) // 1GB
            .minFileSize(1L)
            .allowedExtensions(List.of())
            .requireVirusScan(false)
            .requireOcr(false)
            .build();

        return UploadPolicy.create(DEFAULT_TENANT_ID, "Permissive Policy", rules);
    }

    /**
     * DB에서 복원한 UploadPolicy 생성 (Reconstitute)
     *
     * @param id 정책 ID
     * @param tenantId 테넌트 ID
     * @param policyName 정책 이름
     * @param rules 정책 규칙
     * @param isActive 활성화 여부
     * @param createdAt 생성 시간
     * @param updatedAt 수정 시간
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy reconstitute(
        Long id,
        Long tenantId,
        String policyName,
        UploadPolicy.PolicyRules rules,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return UploadPolicy.reconstitute(
            id,
            tenantId,
            policyName,
            rules,
            isActive,
            createdAt,
            updatedAt
        );
    }

    /**
     * 기본값으로 Reconstitute된 UploadPolicy 생성
     *
     * @param id 정책 ID
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy reconstituteDefault(Long id) {
        UploadPolicy.PolicyRules rules = UploadPolicy.PolicyRules.builder()
            .allowedMimeTypes(List.of("image/jpeg"))
            .maxFileSize(10485760L)
            .minFileSize(1024L)
            .allowedExtensions(List.of(".jpg"))
            .requireVirusScan(true)
            .requireOcr(false)
            .build();

        return UploadPolicy.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_POLICY_NAME,
            rules,
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    /**
     * Builder 패턴으로 PolicyRules 생성
     *
     * @return PolicyRulesBuilder 인스턴스
     */
    public static PolicyRulesBuilder policyRulesBuilder() {
        return new PolicyRulesBuilder();
    }

    /**
     * PolicyRules Builder
     */
    public static class PolicyRulesBuilder {
        private List<String> allowedMimeTypes = List.of("image/jpeg");
        private Long maxFileSize = 10485760L;
        private Long minFileSize = 1024L;
        private List<String> allowedExtensions = List.of(".jpg");
        private Boolean requireVirusScan = true;
        private Boolean requireOcr = false;

        public PolicyRulesBuilder allowedMimeTypes(List<String> allowedMimeTypes) {
            this.allowedMimeTypes = allowedMimeTypes;
            return this;
        }

        public PolicyRulesBuilder maxFileSize(Long maxFileSize) {
            this.maxFileSize = maxFileSize;
            return this;
        }

        public PolicyRulesBuilder minFileSize(Long minFileSize) {
            this.minFileSize = minFileSize;
            return this;
        }

        public PolicyRulesBuilder allowedExtensions(List<String> allowedExtensions) {
            this.allowedExtensions = allowedExtensions;
            return this;
        }

        public PolicyRulesBuilder requireVirusScan(Boolean requireVirusScan) {
            this.requireVirusScan = requireVirusScan;
            return this;
        }

        public PolicyRulesBuilder requireOcr(Boolean requireOcr) {
            this.requireOcr = requireOcr;
            return this;
        }

        public UploadPolicy.PolicyRules build() {
            return UploadPolicy.PolicyRules.builder()
                .allowedMimeTypes(allowedMimeTypes)
                .maxFileSize(maxFileSize)
                .minFileSize(minFileSize)
                .allowedExtensions(allowedExtensions)
                .requireVirusScan(requireVirusScan)
                .requireOcr(requireOcr)
                .build();
        }
    }
}
