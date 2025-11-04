package com.ryuqq.fileflow.domain.policy;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Upload Policy Aggregate Root
 * 테넌트별 파일 업로드 정책 관리
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>우선순위가 낮을수록 먼저 적용</li>
 *   <li>활성 상태의 정책만 평가</li>
 *   <li>정책 규칙은 불변 (Value Object)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadPolicy {

    private final Long id;
    private final TenantId tenantId;
    private final PolicyName policyName;
    private final PolicyRules rules;
    private PolicyStatus status;
    private final Priority priority;

    /**
     * 정책 상태 Enum
     */
    public enum PolicyStatus {
        ACTIVE,
        INACTIVE,
        DEPRECATED
    }

    /**
     * Private 생성자
     *
     * @param id Upload Policy ID (null 가능 - 신규 생성 시)
     * @param tenantId Tenant ID
     * @param policyName 정책 이름
     * @param rules 정책 규칙
     * @param priority 우선순위
     */
    private UploadPolicy(
        Long id,
        TenantId tenantId,
        PolicyName policyName,
        PolicyRules rules,
        Priority priority
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.policyName = policyName;
        this.rules = rules;
        this.status = PolicyStatus.ACTIVE;
        this.priority = priority;
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id Upload Policy ID
     * @param tenantId Tenant ID
     * @param policyName 정책 이름
     * @param rules 정책 규칙
     * @param status 정책 상태
     * @param priority 우선순위
     */
    private UploadPolicy(
        Long id,
        TenantId tenantId,
        PolicyName policyName,
        PolicyRules rules,
        PolicyStatus status,
        Priority priority
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.policyName = policyName;
        this.rules = rules;
        this.status = status;
        this.priority = priority;
    }

    /**
     * Static Factory Method - 신규 Upload Policy 생성
     *
     * @param tenantId Tenant ID
     * @param policyName 정책 이름
     * @param rules 정책 규칙
     * @param priority 우선순위
     * @return 생성된 UploadPolicy (ID = null)
     */
    public static UploadPolicy create(
        TenantId tenantId,
        PolicyName policyName,
        PolicyRules rules,
        Priority priority
    ) {
        return new UploadPolicy(null, tenantId, policyName, rules, priority);
    }

    /**
     * 기본 정책 생성
     *
     * @return 기본 정책
     */
    public static UploadPolicy createDefault() {
        PolicyRules defaultRules = PolicyRules.builder()
            .allowAllMimeTypes()
            .maxFileSize(5L * 1024 * 1024 * 1024)
            .minFileSize(1L)
            .build();

        return new UploadPolicy(
            null,
            TenantId.of(0L),
            PolicyName.of("DEFAULT_POLICY"),
            defaultRules,
            Priority.of(999)
        );
    }

    /**
     * DB에서 조회한 데이터로 UploadPolicy 재구성 (Static Factory Method)
     *
     * @param id Upload Policy ID (필수)
     * @param tenantId Tenant ID
     * @param policyName 정책 이름
     * @param rules 정책 규칙
     * @param status 정책 상태
     * @param priority 우선순위
     * @return 재구성된 UploadPolicy
     * @throws IllegalArgumentException id가 null인 경우
     */
    public static UploadPolicy reconstitute(
        Long id,
        TenantId tenantId,
        PolicyName policyName,
        PolicyRules rules,
        PolicyStatus status,
        Priority priority
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new UploadPolicy(id, tenantId, policyName, rules, status, priority);
    }

    /**
     * 정책 평가
     *
     * @param file 파일 메타데이터
     * @return 평가 결과
     */
    public PolicyEvaluationResult evaluate(FileMetadata file) {
        if (!isActive()) {
            return PolicyEvaluationResult.notApplicable(
                "Policy is not active: " + status
            );
        }

        ValidationResult validation = rules.validate(file);

        if (validation.isValid()) {
            return PolicyEvaluationResult.passed(this.id);
        }

        return PolicyEvaluationResult.failed(
            this.id,
            validation.getViolations()
        );
    }

    /**
     * 정책 활성화
     */
    public void activate() {
        this.status = PolicyStatus.ACTIVE;
    }

    /**
     * 정책 비활성화
     */
    public void deactivate() {
        this.status = PolicyStatus.INACTIVE;
    }

    /**
     * 정책 폐기
     */
    public void deprecate() {
        this.status = PolicyStatus.DEPRECATED;
    }

    /**
     * 활성 상태인지 확인
     *
     * @return 활성 상태이면 true
     */
    public boolean isActive() {
        return status == PolicyStatus.ACTIVE;
    }

    /**
     * Upload Policy ID를 반환합니다.
     *
     * @return Upload Policy ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Tenant ID를 반환합니다.
     *
     * @return Tenant ID
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * 정책 이름을 반환합니다.
     *
     * @return 정책 이름
     */
    public PolicyName getPolicyName() {
        return policyName;
    }

    /**
     * 정책 규칙을 반환합니다.
     *
     * @return 정책 규칙
     */
    public PolicyRules getRules() {
        return rules;
    }

    /**
     * 정책 상태를 반환합니다.
     *
     * @return 정책 상태
     */
    public PolicyStatus getStatus() {
        return status;
    }

    /**
     * 우선순위를 반환합니다.
     *
     * @return 우선순위
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * 동등성을 비교합니다.
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadPolicy that = (UploadPolicy) o;
        return Objects.equals(id, that.id);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return UploadPolicy 정보 문자열
     */
    @Override
    public String toString() {
        return "UploadPolicy{" +
            "id=" + id +
            ", tenantId=" + tenantId +
            ", policyName='" + policyName + '\'' +
            ", status=" + status +
            ", priority=" + priority +
            '}';
    }

    /**
     * Policy Rules Value Object
     * 정책 규칙을 표현하는 불변 객체
     *
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static final class PolicyRules {

        private final Set<String> allowedMimeTypes;
        private final Long maxFileSize;
        private final Long minFileSize;
        private final Set<String> allowedExtensions;
        private final Boolean scanRequired;
        private final Boolean ocrEnabled;

        /**
         * Private 생성자
         *
         * @param builder Builder 인스턴스
         */
        private PolicyRules(Builder builder) {
            this.allowedMimeTypes = Set.copyOf(builder.allowedMimeTypes);
            this.maxFileSize = builder.maxFileSize;
            this.minFileSize = builder.minFileSize;
            this.allowedExtensions = Set.copyOf(builder.allowedExtensions);
            this.scanRequired = builder.scanRequired;
            this.ocrEnabled = builder.ocrEnabled;
        }

        /**
         * 파일 검증
         *
         * @param file 파일 메타데이터
         * @return 검증 결과
         */
        public ValidationResult validate(FileMetadata file) {
            List<String> violations = new ArrayList<>();

            if (!allowedMimeTypes.isEmpty() &&
                !allowedMimeTypes.contains(file.getMimeType().value())) {
                violations.add("허용되지 않은 MIME 타입: " + file.getMimeType().value());
            }

            if (file.getSize().bytes() > maxFileSize) {
                violations.add("최대 파일 크기 초과: " + file.getSize().bytes());
            }

            if (file.getSize().bytes() < minFileSize) {
                violations.add("최소 파일 크기 미만: " + file.getSize().bytes());
            }

            String extension = extractExtension(file.getName().value());
            if (!allowedExtensions.isEmpty() &&
                !allowedExtensions.contains(extension)) {
                violations.add("허용되지 않은 확장자: " + extension);
            }

            return violations.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(violations);
        }

        /**
         * 파일명에서 확장자 추출
         *
         * @param fileName 파일명
         * @return 확장자 (소문자)
         */
        private String extractExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return (lastDot == -1) ? "" : fileName.substring(lastDot + 1).toLowerCase();
        }

        /**
         * Builder 생성
         *
         * @return Builder 인스턴스
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * 허용된 MIME 타입 목록을 반환합니다.
         *
         * @return 허용된 MIME 타입 목록 (불변)
         */
        public Set<String> getAllowedMimeTypes() {
            return allowedMimeTypes;
        }

        /**
         * 최대 파일 크기를 반환합니다.
         *
         * @return 최대 파일 크기 (bytes)
         */
        public Long getMaxFileSize() {
            return maxFileSize;
        }

        /**
         * 최소 파일 크기를 반환합니다.
         *
         * @return 최소 파일 크기 (bytes)
         */
        public Long getMinFileSize() {
            return minFileSize;
        }

        /**
         * 허용된 확장자 목록을 반환합니다.
         *
         * @return 허용된 확장자 목록 (불변)
         */
        public Set<String> getAllowedExtensions() {
            return allowedExtensions;
        }

        /**
         * 바이러스 스캔 필수 여부를 반환합니다.
         *
         * @return 바이러스 스캔 필수 여부
         */
        public Boolean getScanRequired() {
            return scanRequired;
        }

        /**
         * OCR 처리 활성화 여부를 반환합니다.
         *
         * @return OCR 처리 활성화 여부
         */
        public Boolean getOcrEnabled() {
            return ocrEnabled;
        }

        /**
         * Policy Rules Builder
         * Builder 패턴을 사용한 PolicyRules 생성
         *
         * @author Sangwon Ryu
         * @since 1.0.0
         */
        public static class Builder {
            private Set<String> allowedMimeTypes = new HashSet<>();
            private Long maxFileSize = Long.MAX_VALUE;
            private Long minFileSize = 1L;
            private Set<String> allowedExtensions = new HashSet<>();
            private Boolean scanRequired = false;
            private Boolean ocrEnabled = false;

            /**
             * MIME 타입 허용 목록 추가
             *
             * @param types MIME 타입 배열
             * @return Builder 인스턴스
             */
            public Builder allowMimeTypes(String... types) {
                this.allowedMimeTypes.addAll(Arrays.asList(types));
                return this;
            }

            /**
             * 모든 MIME 타입 허용
             *
             * @return Builder 인스턴스
             */
            public Builder allowAllMimeTypes() {
                this.allowedMimeTypes = new HashSet<>();
                return this;
            }

            /**
             * 최대 파일 크기 설정
             *
             * @param size 최대 파일 크기 (bytes)
             * @return Builder 인스턴스
             */
            public Builder maxFileSize(Long size) {
                this.maxFileSize = size;
                return this;
            }

            /**
             * 최소 파일 크기 설정
             *
             * @param size 최소 파일 크기 (bytes)
             * @return Builder 인스턴스
             */
            public Builder minFileSize(Long size) {
                this.minFileSize = size;
                return this;
            }

            /**
             * 확장자 허용 목록 추가
             *
             * @param extensions 확장자 배열
             * @return Builder 인스턴스
             */
            public Builder allowExtensions(String... extensions) {
                this.allowedExtensions.addAll(Arrays.asList(extensions));
                return this;
            }

            /**
             * 바이러스 스캔 필수 설정
             *
             * @return Builder 인스턴스
             */
            public Builder requireScan() {
                this.scanRequired = true;
                return this;
            }

            /**
             * OCR 처리 활성화 설정
             *
             * @return Builder 인스턴스
             */
            public Builder enableOcr() {
                this.ocrEnabled = true;
                return this;
            }

            /**
             * PolicyRules 생성
             *
             * @return PolicyRules 인스턴스
             */
            public PolicyRules build() {
                return new PolicyRules(this);
            }
        }
    }
}
