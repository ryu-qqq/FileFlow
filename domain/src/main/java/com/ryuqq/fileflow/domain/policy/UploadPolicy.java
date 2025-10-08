package com.ryuqq.fileflow.domain.policy;

import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException;
import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException.ViolationType;
import com.ryuqq.fileflow.domain.policy.event.PolicyActivatedEvent;
import com.ryuqq.fileflow.domain.policy.event.PolicyUpdatedEvent;
import com.ryuqq.fileflow.domain.policy.vo.FileAttributes;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 업로드 정책 Aggregate Root
 * 파일 업로드에 대한 모든 정책을 관리하고 검증합니다.
 *
 * 비즈니스 규칙:
 * 1. 정책 업데이트 시 버전 자동 증가
 * 2. 활성화된 정책만 적용 가능
 * 3. 유효 기간 검증 (effectiveFrom < effectiveUntil)
 * 4. 파일 타입별 정책 필수 검증
 */
public final class UploadPolicy {
    private static final int INITIAL_VERSION = 1;

    private final PolicyKey policyKey;
    private final FileTypePolicies fileTypePolicies;
    private final RateLimiting rateLimiting;
    private final int version;
    private final boolean isActive;
    private final LocalDateTime effectiveFrom;
    private final LocalDateTime effectiveUntil;
    private final List<Object> domainEvents;

    private UploadPolicy(
            PolicyKey policyKey,
            FileTypePolicies fileTypePolicies,
            RateLimiting rateLimiting,
            int version,
            boolean isActive,
            LocalDateTime effectiveFrom,
            LocalDateTime effectiveUntil
    ) {
        this.policyKey = policyKey;
        this.fileTypePolicies = fileTypePolicies;
        this.rateLimiting = rateLimiting;
        this.version = version;
        this.isActive = isActive;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
        this.domainEvents = new ArrayList<>();
    }

    /**
     * 새로운 UploadPolicy를 생성합니다.
     *
     * @param policyKey 정책 식별자
     * @param fileTypePolicies 파일 타입별 정책
     * @param rateLimiting Rate Limiting 정책
     * @param effectiveFrom 유효 시작 일시
     * @param effectiveUntil 유효 종료 일시
     * @return UploadPolicy 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 값이 입력된 경우
     */
    public static UploadPolicy create(
            PolicyKey policyKey,
            FileTypePolicies fileTypePolicies,
            RateLimiting rateLimiting,
            LocalDateTime effectiveFrom,
            LocalDateTime effectiveUntil
    ) {
        validatePolicyKey(policyKey);
        validateFileTypePolicies(fileTypePolicies);
        validateRateLimiting(rateLimiting);
        validateEffectivePeriod(effectiveFrom, effectiveUntil);

        return new UploadPolicy(
                policyKey,
                fileTypePolicies,
                rateLimiting,
                INITIAL_VERSION,
                false,
                effectiveFrom,
                effectiveUntil
        );
    }

    /**
     * 기존 정책으로 UploadPolicy를 재구성합니다 (Repository에서 사용).
     *
     * @param policyKey 정책 식별자
     * @param fileTypePolicies 파일 타입별 정책
     * @param rateLimiting Rate Limiting 정책
     * @param version 버전
     * @param isActive 활성 상태
     * @param effectiveFrom 유효 시작 일시
     * @param effectiveUntil 유효 종료 일시
     * @return UploadPolicy 인스턴스
     */
    public static UploadPolicy reconstitute(
            PolicyKey policyKey,
            FileTypePolicies fileTypePolicies,
            RateLimiting rateLimiting,
            int version,
            boolean isActive,
            LocalDateTime effectiveFrom,
            LocalDateTime effectiveUntil
    ) {
        return new UploadPolicy(
                policyKey,
                fileTypePolicies,
                rateLimiting,
                version,
                isActive,
                effectiveFrom,
                effectiveUntil
        );
    }

    /**
     * 파일을 정책에 따라 검증합니다.
     *
     * 검증 항목:
     * 1. 정책이 활성화되어 있는지
     * 2. 현재 시점이 유효 기간 내인지
     * 3. 파일 타입에 대한 정책이 존재하는지
     * 4. 파일 크기가 허용 범위 내인지
     * 5. 파일 개수가 허용 범위 내인지
     *
     * @param fileType 파일 타입
     * @param fileFormat 파일 포맷 (예: "jpg", "png", "pdf", Optional)
     * @param fileSizeBytes 파일 크기 (bytes)
     * @param fileCount 파일 개수
     * @throws PolicyViolationException 정책 위반 시
     */
    public void validateFile(FileType fileType, String fileFormat, long fileSizeBytes, int fileCount) {
        validatePolicyIsActive();
        validateEffectiveNow();
        validateFileTypeSupported(fileType);

        try {
            FileAttributes.Builder builder = FileAttributes.builder()
                .sizeBytes(fileSizeBytes)
                .fileCount(fileCount);

            // fileFormat이 제공되면 사용, 아니면 IMAGE 타입의 경우 기본값 설정
            if (fileFormat != null && !fileFormat.isBlank()) {
                builder.format(fileFormat);
            } else if (fileType == FileType.IMAGE) {
                builder.format("jpg"); // IMAGE 타입 기본 format
            }

            FileAttributes attributes = builder.build();
            fileTypePolicies.validate(fileType, attributes);
        } catch (IllegalArgumentException e) {
            throw new PolicyViolationException(
                    determineViolationType(e.getMessage()),
                    e.getMessage()
            );
        }
    }

    /**
     * Rate Limiting을 검증합니다.
     *
     * @param currentRequestCount 현재 시간당 요청 횟수
     * @param currentUploadCount 현재 일일 업로드 횟수
     * @throws PolicyViolationException Rate Limit 초과 시
     */
    public void validateRateLimit(int currentRequestCount, int currentUploadCount) {
        validatePolicyIsActive();
        validateEffectiveNow();

        if (!rateLimiting.isAllowed(currentRequestCount, currentUploadCount)) {
            throw new PolicyViolationException(
                    ViolationType.RATE_LIMIT_EXCEEDED,
                    String.format("Rate limit exceeded. Current: requests=%d, uploads=%d. Limits: requestsPerHour=%d, uploadsPerDay=%d",
                            currentRequestCount, currentUploadCount,
                            rateLimiting.requestsPerHour(), rateLimiting.uploadsPerDay())
            );
        }
    }

    /**
     * 정책을 업데이트하고 새 버전을 생성합니다.
     *
     * 비즈니스 규칙:
     * 1. 버전이 자동으로 1 증가합니다
     * 2. PolicyUpdatedEvent가 발행됩니다
     * 3. 새로운 인스턴스가 반환됩니다 (불변성)
     *
     * @param newPolicies 새로운 파일 타입 정책
     * @param changedBy 변경자
     * @return 업데이트된 새로운 UploadPolicy 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public UploadPolicy updatePolicy(FileTypePolicies newPolicies, String changedBy) {
        validateFileTypePolicies(newPolicies);
        validateChangedBy(changedBy);

        int newVersion = this.version + 1;

        UploadPolicy updatedPolicy = new UploadPolicy(
                this.policyKey,
                newPolicies,
                this.rateLimiting,
                newVersion,
                this.isActive,
                this.effectiveFrom,
                this.effectiveUntil
        );

        // PolicyUpdatedEvent 발행
        PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                policyKey.getValue(),
                this.version,
                newVersion,
                changedBy,
                LocalDateTime.now()
        );
        updatedPolicy.domainEvents.add(event);

        return updatedPolicy;
    }

    /**
     * 정책을 활성화합니다.
     *
     * 비즈니스 규칙:
     * 1. 이미 활성화된 정책은 다시 활성화할 수 없습니다
     * 2. PolicyActivatedEvent가 발행됩니다
     * 3. 새로운 인스턴스가 반환됩니다 (불변성)
     *
     * @return 활성화된 새로운 UploadPolicy 인스턴스
     * @throws IllegalStateException 이미 활성화된 경우
     */
    public UploadPolicy activate() {
        if (this.isActive) {
            throw new IllegalStateException("Policy is already active");
        }

        UploadPolicy activatedPolicy = new UploadPolicy(
                this.policyKey,
                this.fileTypePolicies,
                this.rateLimiting,
                this.version,
                true,
                this.effectiveFrom,
                this.effectiveUntil
        );

        // PolicyActivatedEvent 발행 (activatedBy는 "SYSTEM"으로 설정)
        PolicyActivatedEvent event = new PolicyActivatedEvent(
                policyKey.getValue(),
                this.version,
                "SYSTEM",
                LocalDateTime.now()
        );
        activatedPolicy.domainEvents.add(event);

        return activatedPolicy;
    }

    /**
     * 정책을 비활성화합니다.
     *
     * @return 비활성화된 새로운 UploadPolicy 인스턴스
     * @throws IllegalStateException 이미 비활성화된 경우
     */
    public UploadPolicy deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Policy is already inactive");
        }

        return new UploadPolicy(
                this.policyKey,
                this.fileTypePolicies,
                this.rateLimiting,
                this.version,
                false,
                this.effectiveFrom,
                this.effectiveUntil
        );
    }

    /**
     * 특정 시점에 정책이 유효한지 확인합니다.
     *
     * @param dateTime 확인할 시점
     * @return 유효 여부
     */
    public boolean isEffectiveAt(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }

        return !dateTime.isBefore(effectiveFrom) && !dateTime.isAfter(effectiveUntil);
    }

    /**
     * 현재 시점에 정책이 유효한지 확인합니다.
     *
     * @return 유효 여부
     */
    public boolean isEffectiveNow() {
        return isEffectiveAt(LocalDateTime.now());
    }

    // ========== Validation Methods ==========

    private void validatePolicyIsActive() {
        if (!this.isActive) {
            throw new PolicyViolationException(
                    ViolationType.INVALID_FORMAT,
                    "Policy is not active"
            );
        }
    }

    private void validateEffectiveNow() {
        if (!isEffectiveNow()) {
            throw new PolicyViolationException(
                    ViolationType.INVALID_FORMAT,
                    String.format("Policy is not effective at current time. Effective period: %s to %s",
                            effectiveFrom, effectiveUntil)
            );
        }
    }

    private void validateFileTypeSupported(FileType fileType) {
        if (!fileTypePolicies.hasPolicyFor(fileType)) {
            throw new PolicyViolationException(
                    ViolationType.INVALID_FORMAT,
                    "No policy found for file type: " + fileType
            );
        }
    }

    private static void validatePolicyKey(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }
    }

    private static void validateFileTypePolicies(FileTypePolicies fileTypePolicies) {
        if (fileTypePolicies == null) {
            throw new IllegalArgumentException("FileTypePolicies cannot be null");
        }
    }

    private static void validateRateLimiting(RateLimiting rateLimiting) {
        if (rateLimiting == null) {
            throw new IllegalArgumentException("RateLimiting cannot be null");
        }
    }

    private static void validateEffectivePeriod(LocalDateTime effectiveFrom, LocalDateTime effectiveUntil) {
        if (effectiveFrom == null) {
            throw new IllegalArgumentException("EffectiveFrom cannot be null");
        }
        if (effectiveUntil == null) {
            throw new IllegalArgumentException("EffectiveUntil cannot be null");
        }
        if (!effectiveFrom.isBefore(effectiveUntil)) {
            throw new IllegalArgumentException("EffectiveFrom must be before EffectiveUntil");
        }
    }

    private static void validateChangedBy(String changedBy) {
        if (changedBy == null || changedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("ChangedBy cannot be null or empty");
        }
    }

    /**
     * 예외 메시지를 기반으로 적절한 ViolationType을 결정합니다.
     *
     * @param errorMessage 예외 메시지
     * @return ViolationType
     */
    private static ViolationType determineViolationType(String errorMessage) {
        if (errorMessage == null) {
            return ViolationType.FILE_SIZE_EXCEEDED;
        }

        String lowerMessage = errorMessage.toLowerCase();

        if (lowerMessage.contains("format not allowed") || lowerMessage.contains("format cannot")) {
            return ViolationType.INVALID_FORMAT;
        }
        if (lowerMessage.contains("file size exceeds") || lowerMessage.contains("size exceeds")) {
            return ViolationType.FILE_SIZE_EXCEEDED;
        }
        if (lowerMessage.contains("file count exceeds") || lowerMessage.contains("count exceeds")) {
            return ViolationType.FILE_COUNT_EXCEEDED;
        }
        if (lowerMessage.contains("dimension exceeds") || lowerMessage.contains("image dimension")) {
            return ViolationType.DIMENSION_EXCEEDED;
        }

        // 기본값
        return ViolationType.FILE_SIZE_EXCEEDED;
    }

    // ========== Domain Events ==========

    /**
     * 도메인 이벤트 목록을 반환합니다.
     *
     * @return 불변 이벤트 리스트
     */
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 도메인 이벤트를 초기화합니다.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========== Getters ==========

    public PolicyKey getPolicyKey() {
        return policyKey;
    }

    public FileTypePolicies getFileTypePolicies() {
        return fileTypePolicies;
    }

    public RateLimiting getRateLimiting() {
        return rateLimiting;
    }

    public int getVersion() {
        return version;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDateTime getEffectiveUntil() {
        return effectiveUntil;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadPolicy that = (UploadPolicy) o;
        return Objects.equals(policyKey, that.policyKey) &&
               version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyKey, version);
    }

    @Override
    public String toString() {
        return "UploadPolicy{" +
                "policyKey=" + policyKey +
                ", version=" + version +
                ", isActive=" + isActive +
                ", effectiveFrom=" + effectiveFrom +
                ", effectiveUntil=" + effectiveUntil +
                ", fileTypePoliciesCount=" + fileTypePolicies.size() +
                ", rateLimiting=" + rateLimiting +
                '}';
    }
}
